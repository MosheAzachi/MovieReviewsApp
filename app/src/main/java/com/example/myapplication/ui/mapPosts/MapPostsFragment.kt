package com.example.myapplication.ui.mapPosts

import Movie
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.databinding.FragmentMapPostsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.example.myapplication.R
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapPostsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapPostsBinding: FragmentMapPostsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mapPostsBinding = FragmentMapPostsBinding.inflate(inflater, container, false)
        val view = mapPostsBinding.root

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts")

        return view
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        // Set a custom info window adapter
        googleMap.setInfoWindowAdapter(PostInfoWindowAdapter())
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Add markers for each post location
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val postLatitude = postSnapshot.child("latitude").getValue(Double::class.java)
                    val postLongitude = postSnapshot.child("longitude").getValue(Double::class.java)

                    postLatitude?.let { lat ->
                        postLongitude?.let { lon ->
                            val postLatLng = LatLng(lat, lon)
                            val postDetails = postSnapshot.getValue(PostDetails::class.java)

                            postDetails?.let { details ->
                                val marker = googleMap.addMarker(
                                    MarkerOptions().position(postLatLng).title("Post Details")
                                )
                                marker.tag = details
                            }
                        }
                    }
                }

                // Move the camera to the first post location (you can customize this)
                if (dataSnapshot.childrenCount > 0) {
                    val firstPostSnapshot = dataSnapshot.children.iterator().next()
                    val firstPostLatitude =
                        firstPostSnapshot.child("latitude").getValue(Double::class.java)
                    val firstPostLongitude =
                        firstPostSnapshot.child("longitude").getValue(Double::class.java)

                    firstPostLatitude?.let { lat ->
                        firstPostLongitude?.let { lon ->
                            val firstPostLatLng = LatLng(lat, lon)
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(firstPostLatLng, 12f)
                            )
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        // Set a listener for marker click events
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    inner class PostInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker?): View? {
            return null // Return null to use default window
        }

        override fun getInfoContents(marker: Marker?): View {
            val view =
                layoutInflater.inflate(R.layout.custom_info_window, null) // Create a custom layout

            val postDetails = marker?.tag as? PostDetails

            postDetails?.let {
                // Customize the content of the info window
                // For example, set text views or other UI elements

                // Example: Set movie title
                val movieTitleTextView: TextView = view.findViewById(R.id.textViewMovieTitle)
                movieTitleTextView.text = "Movie: ${it.movie?.title}"

                // Example: Set review
                val reviewTextView: TextView = view.findViewById(R.id.textViewReview)
                reviewTextView.text = "Review: ${it.review}"

                // Example: Set rating
                val ratingTextView: TextView = view.findViewById(R.id.textViewRating)
                ratingTextView.text = "Rating: ${it.rating}"

                // Add more fields as needed
            }

            return view
        }
    }

    data class PostDetails(
        val userId: String? = null,
        val movie: Movie? = null,
        val review: String? = null,
        val rating: Int? = null,
        val latitude: Double? = null,
        val longitude: Double? = null
    )
}
