package com.example.freshfactory.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val orderDate: String,
    val totalAmount: Double,
    val status: String,
    val deliveryAddress: String,
    val paymentMethod: String,
    val itemsCount: Int,
    val itemsSummary: String
)
