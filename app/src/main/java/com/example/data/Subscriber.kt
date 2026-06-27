package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers")
data class Subscriber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val subscriptionDate: String, // YYYY-MM-DD
    val subscriptionDuration: Int,
    val subscriptionDurationType: String, // "days" or "months"
    val endDate: String, // YYYY-MM-DD calculated
    val deviceNumber: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
