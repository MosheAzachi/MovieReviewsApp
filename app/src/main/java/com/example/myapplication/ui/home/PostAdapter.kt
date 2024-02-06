package com.example.myapplication.ui.home

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Posts
import com.example.myapplication.models.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class PostAdapter(private val postList: List<Posts>,private val navController: NavController) :

    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieImage = itemView.findViewById<ImageView>(R.id.imageMoviePost)
        private val movieTitle = itemView.findViewById<TextView>(R.id.textViewMovieTitlePost)
        private val movieOverview = itemView.findViewById<TextView>(R.id.textViewOverviewPost)
        private val movieRank = itemView.findViewById<TextView>(R.id.textViewRankPost)

        private val userImage = itemView.findViewById<ImageView>(R.id.imageProfileUser)
        private val userName = itemView.findViewById<TextView>(R.id.textViewNamePost)
        private val userAge = itemView.findViewById<TextView>(R.id.textViewAgePost)
        private val userFavorite = itemView.findViewById<TextView>(R.id.textViewFavMoviePost)

        fun bind(post: Posts) {
            try {
                Picasso.get().load("https://image.tmdb.org/t/p/w500${post.movie.poster_path}").into(movieImage)
                movieTitle.text = post.movie.title
                movieOverview.text = Html.fromHtml("<b>Review:</b> ${post.review}", Html.FROM_HTML_MODE_COMPACT)
                movieRank.text = Html.fromHtml("<b>Rank:</b> ${post.rating}/10", Html.FROM_HTML_MODE_COMPACT)
                getUserData(post.userId) { userData ->
                    if (userData != null) {
                        userName.text = Html.fromHtml("<b>Name:</b> ${userData.userName}", Html.FROM_HTML_MODE_COMPACT)
                        userAge.text = Html.fromHtml("<b>Age:</b> ${userData.userAge}", Html.FROM_HTML_MODE_COMPACT)
                        userFavorite.text = Html.fromHtml("<b>Favorite Movie:</b> ${userData.favoriteMovie}", Html.FROM_HTML_MODE_COMPACT)
                        Picasso.get().load(userData.url).into(userImage)
                    }
                }
                movieImage.setOnClickListener{
                    val action = HomeFragmentDirections.actionNavigationHomeToNavigationDetailMovie(
                        post.movie.title.toString(),
                        post.movie.overview.toString(),
                        post.movie.release_date.toString(),
                        post.movie.poster_path.toString(),
                        post.movie.vote_average.toString()
                    )
                    navController.navigate(action)
                }
            } catch (e: Exception) {
                Log.e("PostAdapter", "Error binding post data: ${e.message}")
            }
        }
    }

    private fun getUserData(userId: String, callback: (Users?) -> Unit) {
        val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val userRef: DatabaseReference = firebaseDatabase.reference.child("Users").child(userId)

        // Add a listener for a single value event
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the snapshot has data
                if (snapshot.exists()) {
                    // Convert the snapshot to Users class
                    val userData = snapshot.getValue(Users::class.java)
                    callback(userData)
                } else {
                    // No data found for the given userId
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, if any
                callback(null)
            }
        })
    }
}