package com.example.myapplication.ui.post

import Movie
import MovieResponse
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.FragmentAddPostBinding
import com.example.myapplication.models.Posts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class AddPostFragment : Fragment() {
    private lateinit var addPostBinding: FragmentAddPostBinding
    private var isMovieSelected: Boolean = false
    private var movie: Movie = Movie(null, null, null, null, null, null)

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser: FirebaseUser? = firebaseAuth.currentUser

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRefrence: DatabaseReference = database.reference.child("Posts")
    val newPostRef = myRefrence.push()

    private val apiKey =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzZDg2ODdjODkyYzU5ZmE3YTc4ZDM0OWE2YThmM2U1MyIsInN1YiI6IjY1NzljZmU0NGQyM2RkMDEzYTEyYmJkYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.OpyE_hkKw0mViyZowyb3UxWNUG0szs3vLvhztF13d2Y"

    private val searchDelay: Long = 1000 // Delay in milliseconds

    private var searchJob: Job? = null
    private var isSearchEnabled: Boolean = true

    // Location variables
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

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
                // Request location permission first
                requestLocationPermission()
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
        return addPostBinding.editTextReview.text.isNotEmpty() &&
                addPostBinding.editTextRating.text.isNotEmpty() &&
                movie.title != null
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed to get location
            getLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getLocation() {
        // Check for permission again before using location services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission denied, handle accordingly
            Toast.makeText(
                requireContext(),
                "Location permission denied. Cannot get location.",
                Toast.LENGTH_SHORT
            ).show()
            // Continue with the post creation even if location permission is denied
            createPostWithLocation()
        } else {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location
                    location?.let {
                        userLatitude = it.latitude
                        userLongitude = it.longitude
                        Log.d(
                            "AddPostFragment",
                            "Latitude: $userLatitude, Longitude: $userLongitude"
                        )
                    }

                    // Continue with the post creation
                    createPostWithLocation()
                }
                .addOnFailureListener { e ->
                    Log.e("AddPostFragment", "Error getting location: ${e.message}")

                    // Continue with the post creation even if location retrieval fails
                    createPostWithLocation()
                }
        }
    }

    private fun createPostWithLocation() {
        // Use userLatitude and userLongitude in your Posts object
        val post = Posts(
            currentUser?.uid.toString(),
            movie,
            addPostBinding.editTextReview.text.toString(),
            addPostBinding.editTextRating.text.toString().toInt(),
            userLatitude,
            userLongitude
        )

        newPostRef.setValue(post)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Post has been created",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
    }

    companion object {
        // Request code for location permissions
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}
