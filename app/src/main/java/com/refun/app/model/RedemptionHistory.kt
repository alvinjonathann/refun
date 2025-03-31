package com.refun.app.model

import java.util.Date

data class RedemptionHistory(
    val id: String,
    val points: Int,
    val date: Date,
    val status: String
) 