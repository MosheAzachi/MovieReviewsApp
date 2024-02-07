package com.example.myapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.models.Posts
import com.example.myapplication.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private lateinit var homeBinding: FragmentHomeBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userRef: DatabaseReference = firebaseDatabase.reference.child("Users")
    private val postRef: DatabaseReference = firebaseDatabase.reference.child("Posts")

    private lateinit var postAdapter: PostAdapter
    private val postList: MutableList<Posts> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = homeBinding.root
        if (firebaseAuth.currentUser != null) {
            homeBinding.floatingActionButton.visibility = View.VISIBLE
        } else {
            homeBinding.floatingActionButton.visibility = View.GONE
        }
        homeBinding.floatingActionButton.setOnClickListener {
            userRef.child(firebaseAuth.uid.toString()).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        findNavController().navigate(R.id.action_navigation_home_to_navigation_add_post)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Edit your profile before making a post",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        postAdapter = PostAdapter(postList,findNavController())
        homeBinding.recyclerView.adapter = postAdapter
        homeBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        fetchDataFromDatabase()
        return view
    }
    private fun fetchDataFromDatabase() {
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (i in snapshot.childrenCount - 1 downTo 0) {
                    val postSnapshot = snapshot.children
                    val list = postSnapshot.toList()
                    val post = list.get(i.toInt()).getValue(Posts::class.java)
                    post?.let {
                        postList.add(it)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}