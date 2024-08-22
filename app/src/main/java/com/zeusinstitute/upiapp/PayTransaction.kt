package com.zeusinstitute.upiapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PayTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: String, // "Credit" or "Debit"
    val date: String
)