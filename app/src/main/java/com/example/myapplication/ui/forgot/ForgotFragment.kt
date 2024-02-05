package com.example.myapplication.ui.forgot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentForgotBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotFragment : Fragment() {

    private lateinit var forgotBinding: FragmentForgotBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        forgotBinding = FragmentForgotBinding.inflate(inflater, container, false)
        val view = forgotBinding.root
        forgotBinding.buttonReset.setOnClickListener {
            val email = forgotBinding.editTextTextEmailReset.text.toString()
            if (email.isNotEmpty()) {
                forgotWithFirebase(email)
            }
        }
        return view
    }

    private fun forgotWithFirebase(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                findNavController().navigate(R.id.action_navigation_forgot_to_navigation_login)
                Toast.makeText(
                    requireContext(),
                    "We sent a password reset mail to your mail address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}