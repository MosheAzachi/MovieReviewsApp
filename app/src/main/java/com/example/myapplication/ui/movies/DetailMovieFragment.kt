package com.example.myapplication.ui.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.myapplication.databinding.FragmentDetailMovieBinding
import com.squareup.picasso.Picasso

class DetailMovieFragment : Fragment() {

    private lateinit var binding: FragmentDetailMovieBinding

    private val args: DetailMovieFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = args.movieTitle
        val review = args.movieReview
        val movieUrl=args.moviePosterPath
        val voteAvg=args.movieVoteAverage
        val movieRelease=args.movieRelease
        if (movieUrl != null) {
            Picasso.get().load("https://image.tmdb.org/t/p/w500${movieUrl}")
                .into(binding.imageView)
        }
        binding.textViewTitle.text = "Title: "+title
        binding.textViewOverview.text = "Overview: "+review
        binding.textViewVote.text = "Vote: "+voteAvg
        binding.textViewRelease.text = "Release Date: "+movieRelease

    }
}
