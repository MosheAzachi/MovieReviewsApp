package com.example.myapplication.ui.userPosts

import UserPostsAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentUserPostsBinding
import com.example.myapplication.models.Posts
import com.example.myapplication.ui.movies.MoviesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.squareup.picasso.Picasso


class UserPostsFragment : Fragment() {
    lateinit var userPostBinding: FragmentUserPostsBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser: FirebaseUser? = firebaseAuth.currentUser

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val postRef: DatabaseReference = firebaseDatabase.reference.child("Posts")

    private val postList = ArrayList<Posts>()
    private val postListKey = ArrayList<String>()

    private lateinit var userPostsAdapter: UserPostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userPostBinding = FragmentUserPostsBinding.inflate(inflater, container, false)
        val view = userPostBinding.root
        setupRecyclerView()
        getAndSetData()
        return view
    }

    private fun getAndSetData() {
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear and repopulate the dataset when data changes
                postList.clear()
                postListKey.clear()

                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Posts::class.java)

                    // Check if the post's userId is equal to currentUser.uid
                    if (post != null && post.userId == currentUser?.uid) {
                        postList.add(post)
                        postListKey.add(postSnapshot.key.toString())
                    }
                }

                userPostsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

    private fun setupRecyclerView() {
        userPostBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            userPostsAdapter = UserPostsAdapter(postList, postListKey, postRef)
            adapter = userPostsAdapter
        }
    }
}