import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.UpdatePostItemBinding
import com.example.myapplication.models.Posts
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso


class UserPostsAdapter(
    private val postsList: List<Posts>,
    private val postsListKey: List<String>,
    private val postRef: DatabaseReference
) :
    RecyclerView.Adapter<UserPostsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: UpdatePostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Posts, key: String) {
            binding.apply {
                Picasso.get().load("https://image.tmdb.org/t/p/w500${post.movie.poster_path}")
                    .into(binding.imageMoviePost)
                binding.editTextUpdateMovieReview.setText(post.review)
                binding.editTextUpdateMovieRank.setText(post.rating.toString())
                binding.textViewMovieTitlePost.text = post.movie.title
                binding.buttonUpdatePost.setOnClickListener {
                    val ratingValue = binding.editTextUpdateMovieRank.text.toString().toInt()
                    if (ratingValue in 1..10) {
                        postRef.child(key).apply {
                            child("review").setValue(binding.editTextUpdateMovieReview.text.toString())
                            child("rating").setValue(
                                ratingValue
                            )
                        }
                    }
                }
                binding.buttonDeletePost.setOnClickListener {
                    postRef.child(key).removeValue()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            UpdatePostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postsList[position]
        val postKey = postsListKey[position]
        holder.bind(post, postKey)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}
