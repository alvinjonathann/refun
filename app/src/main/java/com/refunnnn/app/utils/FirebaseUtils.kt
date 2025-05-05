package com.refunnnn.app.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseUtils {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Authentication
    suspend fun signIn(email: String, password: String) = auth.signInWithEmailAndPassword(email, password).await()

    suspend fun createUser(email: String, password: String) = auth.createUserWithEmailAndPassword(email, password).await()

    fun signOut() = auth.signOut()

    fun getCurrentUser() = auth.currentUser

    // Firestore
    suspend fun createUserProfile(userId: String, name: String, email: String) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "points" to 0,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId).set(userData).await()
    }

    suspend fun getUserProfile(userId: String) = firestore.collection("users").document(userId).get().await()

    suspend fun updateUserPoints(userId: String, points: Int) {
        firestore.collection("users").document(userId).update("points", points).await()
    }

    suspend fun addScanHistory(userId: String, barcode: String, points: Int) {
        val historyData = hashMapOf(
            "barcode" to barcode,
            "points" to points,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId).collection("history").add(historyData).await()
    }

    suspend fun getScanHistory(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("history")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .get()
        .await()
} 