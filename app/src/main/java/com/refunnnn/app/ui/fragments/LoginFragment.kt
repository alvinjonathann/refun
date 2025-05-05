package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.refunnnn.app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import android.text.TextUtils

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.homeFragment)
            return
        }

        emailInput = view.findViewById(R.id.emailInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        emailLayout = view.findViewById(R.id.emailLayout)
        passwordLayout = view.findViewById(R.id.passwordLayout)

        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)
        val registerLink = view.findViewById<View>(R.id.registerLink)

        loginButton.setOnClickListener {
            if (validateForm()) {
                loginUser()
            }
        }

        registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = emailInput.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailLayout.error = "Email is required"
            valid = false
        } else {
            emailLayout.error = null
        }

        val password = passwordInput.text.toString()
        if (TextUtils.isEmpty(password)) {
            passwordLayout.error = "Password is required"
            valid = false
        } else {
            passwordLayout.error = null
        }

        return valid
    }

    private fun loginUser() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
} 