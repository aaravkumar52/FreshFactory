package com.example.freshfactory.CartScreen

data class CartItem(
    val id: Int,
    val name: String,
    val price: String,
    val originalPrice: String,
    val imageResId: Int,
    val quantity: Int = 1
)