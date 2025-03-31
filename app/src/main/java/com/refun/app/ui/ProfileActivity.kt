package com.refun.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.refun.app.databinding.ActivityProfileBinding

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityProfileBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set up back button
            binding.backButton.setOnClickListener {
                finish()
            }

            // Load user data
            loadUserData()

            // Set up save button
            binding.saveButton.setOnClickListener {
                saveUserData()
            }

            // Set up logout button
            binding.logoutButton.setOnClickListener {
                logout()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadUserData() {
        try {
            // Get user data from session
            val userName = sessionManager.getUserName() ?: "User"
            val userEmail = sessionManager.getUserEmail() ?: ""

            // Set the data in the UI
            binding.usernameInput.setText(userName)
            binding.emailInput.setText(userEmail)

            // TODO: Load phone number from local storage or backend
            // For now, we'll leave it empty
            binding.phoneInput.setText("")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data", e)
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        try {
            val username = binding.usernameInput.text.toString()
            val phoneNumber = binding.phoneInput.text.toString()

            // Validate input
            if (username.isEmpty()) {
                binding.usernameLayout.error = "Username is required"
                return
            }

            if (phoneNumber.isNotEmpty() && !isValidPhoneNumber(phoneNumber)) {
                binding.phoneLayout.error = "Please enter a valid phone number"
                return
            }

            // TODO: Save to backend or local storage
            // For now, we'll just update the session
            sessionManager.createLoginSession(sessionManager.getUserEmail() ?: "", username)

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data", e)
            Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        try {
            // Clear the session
            sessionManager.logout()

            // Navigate to login screen
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic phone number validation
        return phoneNumber.matches(Regex("^[+]?[0-9]{10,13}$"))
    }
} 