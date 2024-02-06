package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.home.HomeFragmentDirections
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_profile,
                R.id.navigation_login,
                R.id.navigation_movies
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        if (firebaseAuth.currentUser == null) {
            binding.navView.menu.clear()
            binding.navView.inflateMenu(R.menu.bottom_nav_menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (firebaseAuth.currentUser != null) {
            menuInflater.inflate(R.menu.menu_layout, menu)
            return true
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.signOut) {
            firebaseAuth.signOut()
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        if (item.itemId == R.id.myPosts) {
            navController.navigate(R.id.navigation_user_posts)
            return true
        }
        if (item.itemId==R.id.postsMap) {
            navController.navigate(R.id.navigation_mapPosts)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}