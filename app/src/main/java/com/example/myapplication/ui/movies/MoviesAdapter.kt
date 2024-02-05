package com.example.myapplication.ui.movies

import Movie
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.MovieItemBinding
import com.squareup.picasso.Picasso

class MoviesAdapter(private var movies: List<Movie>, private val navController: NavController) :
    RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    class ViewHolder(val binding: MovieItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        if (movie.poster_path != null) {
            Picasso.get().load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
                .into(holder.binding.MovieImage)
        }
        holder.binding.MovieImage.setOnClickListener {
            val action = MoviesFragmentDirections.actionNavigationMoviesToNavigationDetailMovie(
                movie.title.toString(),
                movie.overview.toString(),
                movie.release_date.toString(),
                movie.poster_path.toString(),
                movie.vote_average.toString()
            )
            navController.navigate(action)
        }
        holder.binding.textViewMovieTitle.text = movie.title
    }

    override fun getItemCount(): Int = movies.size

    // Method to update the dataset and notify the adapter
    fun setData(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}