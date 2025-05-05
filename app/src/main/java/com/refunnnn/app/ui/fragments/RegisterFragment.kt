package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refunnnn.app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import android.text.TextUtils
import java.util.regex.Pattern

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.homeFragment)
            return
        }

        // Initialize views
        try {
            usernameInput = view.findViewById(R.id.usernameInput)
            emailInput = view.findViewById(R.id.emailInput)
            passwordInput = view.findViewById(R.id.passwordInput)
            usernameLayout = view.findViewById(R.id.usernameLayout)
            emailLayout = view.findViewById(R.id.emailLayout)
            passwordLayout = view.findViewById(R.id.passwordLayout)

            val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)
            val loginLink = view.findViewById<View>(R.id.loginLink)

            registerButton.setOnClickListener {
                if (validateForm()) {
                    registerUser()
                }
            }

            loginLink.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val username = usernameInput.text.toString()
        if (TextUtils.isEmpty(username)) {
            usernameLayout.error = "Username is required"
            valid = false
        } else {
            usernameLayout.error = null
        }

        val email = emailInput.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailLayout.error = "Email is required"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email"
            valid = false
        } else {
            emailLayout.error = null
        }

        val password = passwordInput.text.toString()
        if (TextUtils.isEmpty(password)) {
            passwordLayout.error = "Password is required"
            valid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            valid = false
        } else if (!isValidPassword(password)) {
            passwordLayout.error = "Password must contain at least one number"
            valid = false
        } else {
            passwordLayout.error = null
        }

        return valid
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern = Pattern.compile(".*\\d.*")
        return pattern.matcher(password).matches()
    }

    private fun registerUser() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        val username = usernameInput.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email
                        )

                        firestore.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().navigate(R.id.homeFragment)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Error saving user data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
} 