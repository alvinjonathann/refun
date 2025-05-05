package com.refunnnn.app.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun validateAndProcessBarcode(barcode: String): Result<BarcodeValidationResult> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            val doc = db.collection("bottle_barcodes").document(barcode).get().await()
            
            if (!doc.exists()) {
                return Result.success(BarcodeValidationResult.InvalidBarcode)
            }
            
            val isUsed = doc.getBoolean("is_used") ?: false
            if (isUsed) {
                return Result.success(BarcodeValidationResult.AlreadyUsed)
            }
            
            val point = doc.getLong("point") ?: 0L
            val name = doc.getString("name") ?: "Unknown"
            
            // Update barcode status
            db.collection("bottle_barcodes").document(barcode)
                .update(
                    mapOf(
                        "is_used" to true,
                        "user_id" to currentUserId,
                        "scanned_at" to FieldValue.serverTimestamp()
                    )
                ).await()
            
            // Add to scan history
            val scanHistory = hashMapOf(
                "barcode" to barcode,
                "user_id" to currentUserId,
                "point" to point,
                "timestamp" to FieldValue.serverTimestamp(),
                "item_name" to name
            )
            db.collection("scan_history").add(scanHistory).await()
            
            // Update user points
            db.collection("users").document(currentUserId)
                .update("points", FieldValue.increment(point)).await()
            
            Result.success(BarcodeValidationResult.Success(point, name))
        } catch (e: Exception) {
            Log.e("FirestoreUtils", "Error processing barcode: ${e.message}")
            Result.failure(e)
        }
    }
}

sealed class BarcodeValidationResult {
    data class Success(val points: Long, val itemName: String) : BarcodeValidationResult()
    object InvalidBarcode : BarcodeValidationResult()
    object AlreadyUsed : BarcodeValidationResult()
} 