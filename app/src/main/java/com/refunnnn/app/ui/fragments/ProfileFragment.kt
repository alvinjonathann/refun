package com.refunnnn.app.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refunnnn.app.R
import com.refunnnn.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load user data
        loadUserData()

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            showEditUsernameDialog()
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    // Kembali ke LoginActivity
                    val intent = Intent(requireContext(), com.refunnnn.app.ui.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Apakah Anda yakin ingin menghapus akun? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        if (user != null) {
            // Hapus data user dari Firestore
            firestore.collection("users").document(user.uid)
                .delete()
                .addOnSuccessListener {
                    // Hapus akun dari Firebase Auth
                    user.delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                            // Kembali ke LoginActivity
                            val intent = Intent(requireContext(), com.refunnnn.app.ui.LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Gagal menghapus akun: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal menghapus data user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                binding.txtUsername.text = doc.getString("username") ?: "-"
                binding.txtEmail.text = doc.getString("email") ?: "-"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditUsernameDialog() {
        val context = requireContext()
        val editText = EditText(context)
        editText.setText(binding.txtUsername.text)
        editText.setSelection(editText.text.length)
        editText.hint = "Masukkan username baru"
        editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        val padding = (16 * resources.displayMetrics.density).toInt()
        editText.setPadding(padding, padding, padding, padding)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Username")
            .setView(editText)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val newUsername = editText.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    updateUsername(newUsername)
                    dialog.dismiss()
                } else {
                    editText.error = "Username tidak boleh kosong"
                }
            }
        }
        dialog.show()
    }

    private fun updateUsername(newUsername: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("username", newUsername)
            .addOnSuccessListener {
                binding.txtUsername.text = newUsername
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
            }
    }
} 