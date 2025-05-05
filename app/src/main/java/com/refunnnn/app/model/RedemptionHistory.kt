package com.refunnnn.app.model

data class RedemptionHistory(
    val id: String = "",
    val transactionId: String = "",
    val bottleId: String = "",
    val point: Long = 0,
    val userId: String = "",
    val timestamp: Long = 0
) 