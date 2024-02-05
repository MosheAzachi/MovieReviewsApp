package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var loginBinding: FragmentLoginBinding

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = loginBinding.root
        loginBinding.buttonSignin.setOnClickListener {
            val userEmail = loginBinding.editTextTextEmailSignin.text.toString()
            val userPassword = loginBinding.editTextTextPasswordSignin.text.toString()
            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                signWithFirebase(userEmail, userPassword)
            }
        }
        loginBinding.buttonSignup.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_login_to_navigation_signup)
        }
        loginBinding.buttonForgot.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_login_to_navigation_forgot)
        }
        return view
    }

    private fun signWithFirebase(userEmail: String, userPassword: String) {
        firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Login successful")
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    showToast("Login Failed: ${task.exception?.message}")
                }
            }

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
