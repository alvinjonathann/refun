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
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var nameView: TextInputEditText
    private lateinit var emailView: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var logoutButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameView = view.findViewById(R.id.profileName)
        emailView = view.findViewById(R.id.profileEmail)
        saveButton = view.findViewById(R.id.saveProfileButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        // Make email field non-editable
        emailView.isEnabled = false

        // Load user data
        loadUserData()

        saveButton.setOnClickListener {
            saveUserData()
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                nameView.setText(doc.getString("username") ?: "")
                emailView.setText(doc.getString("email") ?: "")
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return
        val newName = nameView.text.toString()

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(userId)
            .update("username", newName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutUser() {
        auth.signOut()
        findNavController().navigate(R.id.loginFragment)
    }
} 