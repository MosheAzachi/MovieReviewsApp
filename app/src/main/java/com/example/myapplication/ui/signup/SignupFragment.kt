package com.example.myapplication.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {

    private lateinit var signupBinding: FragmentSignupBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        signupBinding = FragmentSignupBinding.inflate(inflater, container, false)
        val view = signupBinding.root
        signupBinding.buttonSignupUser.setOnClickListener {
            val userEmail = signupBinding.editTextTextEmailSignup.text.toString()
            val userPassword = signupBinding.editTextTextPasswordSignup.text.toString()
            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                signupWithFirebase(userEmail, userPassword)
            }
        }
        return view
    }

    private fun signupWithFirebase(userEmail: String, userPassword: String) {
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Registration Successful")
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent);
                } else {
                    showToast("Registration Failed: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}