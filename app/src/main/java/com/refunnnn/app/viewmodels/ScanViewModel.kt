package com.refunnnn.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refunnnn.app.utils.FirestoreUtils
import com.refunnnn.app.utils.BarcodeValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {
    private val firestoreUtils = FirestoreUtils()
    
    private val _scanResult = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanResult: StateFlow<ScanResult> = _scanResult
    
    fun processBarcode(barcode: String) {
        viewModelScope.launch {
            _scanResult.value = ScanResult.Loading
            try {
                val result = firestoreUtils.validateAndProcessBarcode(barcode)
                result.onSuccess { validationResult ->
                    _scanResult.value = when (validationResult) {
                        is BarcodeValidationResult.Success -> 
                            ScanResult.Success(validationResult.points, validationResult.itemName)
                        BarcodeValidationResult.InvalidBarcode -> ScanResult.InvalidBarcode
                        BarcodeValidationResult.AlreadyUsed -> ScanResult.AlreadyUsed
                    }
                }.onFailure { exception ->
                    _scanResult.value = ScanResult.Error(exception.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ScanResult {
    object Idle : ScanResult()
    object Loading : ScanResult()
    data class Success(val points: Long, val itemName: String) : ScanResult()
    object InvalidBarcode : ScanResult()
    object AlreadyUsed : ScanResult()
    data class Error(val message: String) : ScanResult()
} 