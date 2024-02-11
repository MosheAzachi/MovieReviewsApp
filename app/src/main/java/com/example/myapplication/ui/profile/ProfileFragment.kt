package com.example.myapplication.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class ProfileFragment : Fragment() {

    private lateinit var profileBinding: FragmentProfileBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser: FirebaseUser? = firebaseAuth.currentUser

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userRef: DatabaseReference = firebaseDatabase.reference.child("Users")

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null

    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageReference: StorageReference = firebaseStorage.reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = profileBinding.root
        registerActivityForResult()

        profileBinding.userProfileImage.setOnClickListener {
            chooseImage()
        }
        profileBinding.buttonUpdateUser.setOnClickListener {
            if (validateData()) uploadPhoto()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getAndSetData()
    }

    private fun getAndSetData() {
        userRef.child(currentUser?.uid.toString()).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("userName").getValue(String::class.java)
                    val userAge = dataSnapshot.child("userAge").getValue(Int::class.java).toString()
                    val userFavorite =
                        dataSnapshot.child("favoriteMovie").getValue(String::class.java)
                    val url = dataSnapshot.child("url").getValue(String::class.java)
                    profileBinding.editTextUpdateName.setText(userName)
                    profileBinding.editTextUpdateAge.setText(userAge)
                    profileBinding.editTextUpdateFavorite.setText(userFavorite)
                    Picasso.get().load(url).into(profileBinding.userProfileImage)

                }
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activityResultLauncher.launch(intent)
    }

    private fun registerActivityForResult() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val resultCode = result.resultCode
                val imageData = result.data
                if (resultCode == RESULT_OK && imageData != null) {
                    imageUri = imageData.data
                    imageUri?.let {
                        Picasso.get().load(it).into(profileBinding.userProfileImage)
                    }
                }
            }
    }

    private fun uploadPhoto() {
        profileBinding.buttonUpdateUser.isClickable = false
        val imgName = currentUser?.uid.toString()
        val imgRef = storageReference.child("images").child(imgName)
        if (imageUri!=null) {
            imageUri?.let { uri ->
                imgRef.putFile(uri).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Image uploaded", Toast.LENGTH_SHORT).show()
                    val myUploadedImageReference = storageReference.child("images").child(imgName)
                    myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->
                        val imgURL = url.toString()
                        changeData(imgURL, imgName)
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            imgRef.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    changeData(downloadUrl,imgName)
                }
        }
    }

    private fun changeData(imgUrl: String, imgName: String) {
        val updatedName = profileBinding.editTextUpdateName.text.toString()
        val updatedAge = profileBinding.editTextUpdateAge.text.toString().toInt()
        val updatedFavorite = profileBinding.editTextUpdateFavorite.text.toString()
        val userId = currentUser?.uid.toString()
        val userMap = mutableMapOf<String, Any>()
        userMap["userId"] = userId
        userMap["userName"] = updatedName
        userMap["userAge"] = updatedAge
        userMap["favoriteMovie"] = updatedFavorite
        userMap["url"] = imgUrl
        userMap["imageName"] = imgName
        userRef.child(userId).updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "The user has been updated", Toast.LENGTH_SHORT)
                    .show()
                profileBinding.buttonUpdateUser.isClickable = true
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun validateData(): Boolean {
        return profileBinding.editTextUpdateName.text != null && profileBinding.editTextUpdateAge.text != null && profileBinding.editTextUpdateFavorite.text != null
    }
}