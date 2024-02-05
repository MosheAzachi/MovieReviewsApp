package com.example.myapplication.ui.post

import Movie
import MovieResponse
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentAddPostBinding
import com.example.myapplication.models.Posts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID

class AddPostFragment : Fragment() {
    private lateinit var addPostBinding: FragmentAddPostBinding
    private var isMovieSelected: Boolean = false
    private var movie: Movie = Movie(null, null, null, null, null, null)

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser: FirebaseUser? = firebaseAuth.currentUser

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRefrence: DatabaseReference = database.reference.child("Posts")

    private val apiKey =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzZDg2ODdjODkyYzU5ZmE3YTc4ZDM0OWE2YThmM2U1MyIsInN1YiI6IjY1NzljZmU0NGQyM2RkMDEzYTEyYmJkYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.OpyE_hkKw0mViyZowyb3UxWNUG0szs3vLvhztF13d2Y"

    private val searchDelay: Long = 1000 // Delay in milliseconds

    private var searchJob: Job? = null
    private var isSearchEnabled: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addPostBinding = FragmentAddPostBinding.inflate(inflater, container, false)
        val view = addPostBinding.root
        val adapter = MovieAdapter { selectedMovie ->
            movie = selectedMovie
            isMovieSelected = true
            updateRecyclerViewVisibility()
            cancelSearchJob()
            disableSearch()
            updateEditTextsVisibility()
        }
        addPostBinding.recyclerViewMovies.layoutManager = LinearLayoutManager(context)
        addPostBinding.recyclerViewMovies.adapter = adapter
        addPostBinding.editTextSearchMovieAddPost.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                cancelSearchJob()
                searchJob = Job() // Create a new job for the next search

                GlobalScope.launch(Dispatchers.Main) {
                    delay(searchDelay) // Add a delay before starting the search
                    val searchQuery = s.toString()
                    if (searchQuery.isNotEmpty() && isSearchEnabled) {
                        // Reset the flag when the text changes
                        isMovieSelected = false
                        updateRecyclerViewVisibility()
                        searchAndSetInApi(searchQuery, adapter)
                    }
                }
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }
        })
        addPostBinding.editTextSearchMovieAddPost.setOnFocusChangeListener { _, hasFocus ->
            isSearchEnabled = hasFocus
            if (!hasFocus) {
                addPostBinding.editTextSearchMovieAddPost.setText(movie.title)
            }
        }
        addPostBinding.buttonCreatePost.setOnClickListener {
            if (validateData()) {
                val post = Posts(
                    currentUser?.uid.toString(),
                    movie,
                    addPostBinding.editTextReview.text.toString(),
                    addPostBinding.editTextRating.text.toString().toInt()
                )
                myRefrence.child(UUID.randomUUID().toString()).setValue(post)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Post has been created",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
            }
        }
        return view
    }

    private fun searchAndSetInApi(searchQuery: String, adapter: MovieAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.themoviedb.org/3/search/movie?query=$searchQuery")
                    .get()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    val moshi = Moshi.Builder()
                        .addLast(KotlinJsonAdapterFactory())
                        .build()
                    val jsonAdapter = moshi.adapter(MovieResponse::class.java)
                    val movieResponse = jsonAdapter.fromJson(responseBody)
                    withContext(Dispatchers.Main) {
                        // Update the RecyclerView with the search results
                        adapter.submitList(movieResponse?.results)
                    }
                }
            } catch (e: Exception) {
                Log.e("MoviesFragment", "Error: ${e.message}")
            }
        }
    }

    private fun updateRecyclerViewVisibility() {
        addPostBinding.recyclerViewMovies.visibility =
            if (isMovieSelected) View.GONE else View.VISIBLE
        updateEditTextsVisibility()
    }

    private fun updateEditTextsVisibility() {
        val visibility = if (isMovieSelected) View.VISIBLE else View.GONE

        addPostBinding.editTextReview.visibility = visibility
        addPostBinding.editTextRating.visibility = visibility
        addPostBinding.buttonCreatePost.visibility = visibility
    }

    private fun cancelSearchJob() {
        searchJob?.cancel()
    }

    private fun disableSearch() {
        isSearchEnabled = false
        addPostBinding.editTextSearchMovieAddPost.clearFocus()
    }

    private fun validateData(): Boolean {
        return addPostBinding.editTextReview.text.isNotEmpty() && addPostBinding.editTextRating.text.isNotEmpty() && movie.title != null
    }

}
