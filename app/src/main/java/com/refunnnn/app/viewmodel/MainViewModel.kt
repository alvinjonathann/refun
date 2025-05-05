package com.refunnnn.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _userPoints = MutableLiveData<Int>()
    val userPoints: LiveData<Int> = _userPoints

    private val _bottlesCollected = MutableLiveData<Int>()
    val bottlesCollected: LiveData<Int> = _bottlesCollected

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            _userPoints.value = document.getLong("points")?.toInt() ?: 0
                            _bottlesCollected.value = document.getLong("bottlesCollected")?.toInt() ?: 0
                            _userName.value = document.getString("name") ?: "User"
                        }
                    }
            }
        }
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            // TODO: Implement barcode validation and point calculation
            // For now, just increment points and bottles
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val currentPoints = _userPoints.value ?: 0
                val currentBottles = _bottlesCollected.value ?: 0
                
                val newPoints = currentPoints + 10
                val newBottles = currentBottles + 1

                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "points" to newPoints,
                            "bottlesCollected" to newBottles
                        )
                    )
                    .addOnSuccessListener {
                        _userPoints.value = newPoints
                        _bottlesCollected.value = newBottles
                    }
            }
        }
    }
} 