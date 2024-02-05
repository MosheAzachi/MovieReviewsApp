package com.example.myapplication.ui.movies

import MovieResponse
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentMoviesBinding
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MoviesFragment : Fragment() {

    private lateinit var moviesBinding: FragmentMoviesBinding
    private lateinit var moviesAdapter: MoviesAdapter
    private val apiKey =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzZDg2ODdjODkyYzU5ZmE3YTc4ZDM0OWE2YThmM2U1MyIsInN1YiI6IjY1NzljZmU0NGQyM2RkMDEzYTEyYmJkYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.OpyE_hkKw0mViyZowyb3UxWNUG0szs3vLvhztF13d2Y"
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        moviesBinding = FragmentMoviesBinding.inflate(inflater, container, false)
        val view = moviesBinding.root

        // Set up the RecyclerView and Adapter
        moviesAdapter = MoviesAdapter(emptyList(), findNavController())
        moviesBinding.recyclerView.adapter = moviesAdapter

        // Add a text changed listener to the EditText for automatic searching
        moviesBinding.editTextSearchMovie.addTextChangedListener { editable ->
            val searchQuery = editable.toString().trim()
            // Cancel the previous search job if any
            searchJob?.cancel()
            // Start a new search job with a delay
            searchJob = GlobalScope.launch(Dispatchers.Main) {
                delay(500) // You can adjust the delay time
                if (searchQuery.isNotEmpty()) {
                    // Trigger the search
                    searchAndSetInApi(searchQuery)
                }
            }
        }

        return view
    }

    private fun searchAndSetInApi(searchQuery: String) {
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
                        moviesAdapter.setData(movieResponse?.results ?: emptyList())
                    }
                } else {
                    Log.e("MoviesFragment", "Error: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("MoviesFragment", "Error: ${e.message}")
            }
        }
    }
}
