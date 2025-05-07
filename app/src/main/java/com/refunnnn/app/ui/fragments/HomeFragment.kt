package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refunnnn.app.R

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val userName = view.findViewById<TextView>(R.id.userName)
        val userPoints = view.findViewById<TextView>(R.id.userPoints)

        // Listen for real-time updates to user points
        firestore.collection("users").document(userId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    userName.text = doc.getString("username") ?: "User"
                    val points = doc.getLong("point") ?: 0
                    userPoints.text = "$points ReFun Poin"
                }
            }

        val tukarPoin = view.findViewById<View>(R.id.quickActionTukarPoin)
        val cekHarga = view.findViewById<View>(R.id.quickActionCekHarga)

        tukarPoin.setOnClickListener {
            findNavController().navigate(R.id.giftCatalogueFragment)
        }

        cekHarga.setOnClickListener {
            val bundle = Bundle().apply { putBoolean("fromCekHarga", true) }
            findNavController().navigate(R.id.scanFragment, bundle)
        }
    }
} 