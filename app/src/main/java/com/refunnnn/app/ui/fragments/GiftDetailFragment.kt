package com.refunnnn.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.refunnnn.app.R
import com.refunnnn.app.model.Gift

class GiftDetailFragment : Fragment() {
    private var gift: Gift? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gift_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val giftId = arguments?.getString("giftId") ?: return
        val img = view.findViewById<ImageView>(R.id.giftImageDetail)
        val title = view.findViewById<TextView>(R.id.giftNameText)
        val desc = view.findViewById<TextView>(R.id.giftDescText)
        val points = view.findViewById<TextView>(R.id.giftPointsText)
        val redeemButton = view.findViewById<Button>(R.id.redeemButton)
        val btnBack = view.findViewById<ImageView>(R.id.btnBackGiftDetail)

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (giftId == "dummy1") {
            // Data dummy langsung dari kode
            gift = Gift(
                id = "dummy1",
                title = "Voucher gopay Rp 10.000",
                points = 50,
                imageUrl = "",
                description = "Voucher gopay senilai Rp 10.000"
            )
            img.setImageResource(R.drawable.voucher_gopay)
            title.text = gift?.title
            desc.text = gift?.description
            points.text = "${gift?.points ?: 0} points"
        } else {
            // Ambil detail hadiah dari Firestore
            FirebaseFirestore.getInstance().collection("gifts").document(giftId)
                .get().addOnSuccessListener { doc ->
                    gift = Gift(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        points = (doc.getLong("points") ?: 0L).toInt(),
                        imageUrl = doc.getString("imageUrl") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                    img.setImageResource(R.drawable.voucher_gopay)
                    title.text = gift?.title
                    desc.text = gift?.description
                    points.text = "${gift?.points ?: 0} points"
                }
        }

        redeemButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val giftData = gift ?: return@setOnClickListener
            redeemGift(userId, giftData, 1, // qty = 1
                onSuccess = {
                    Toast.makeText(requireContext(), "Redeem successful!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                },
                onFailure = {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun redeemGift(
        userId: String,
        gift: Gift,
        qty: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        val redemptionRef = db.collection("redemptions").document()
        val totalPoints = qty * gift.points

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("point") ?: 0L
            if (currentPoints >= totalPoints) {
                transaction.update(userRef, "point", currentPoints - totalPoints)
                transaction.set(redemptionRef, mapOf(
                    "userId" to userId,
                    "giftId" to gift.id,
                    "giftTitle" to gift.title,
                    "quantity" to qty,
                    "totalPoints" to totalPoints,
                    "timestamp" to FieldValue.serverTimestamp()
                ))
            } else {
                throw FirebaseFirestoreException("Not enough points", FirebaseFirestoreException.Code.ABORTED)
            }
        }.addOnSuccessListener { onSuccess() }
         .addOnFailureListener { e -> onFailure(e.message ?: "Unknown error") }
    }
} 