package com.example.freshfactory.CartScreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(item: CartItem) {
        _cartItems.update { currentItems ->
            val index = currentItems.indexOfFirst { it.id == item.id }
            if (index != -1) {
                currentItems.mapIndexed { i, existingItem ->
                    if (i == index) existingItem.copy(quantity = existingItem.quantity + 1)
                    else existingItem
                }
            } else {
                currentItems + item
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.id != item.id }
        }
    }

    fun updateQuantity(item: CartItem, delta: Int) {
        _cartItems.update { currentItems ->
            currentItems.map { existingItem ->
                if (existingItem.id == item.id) {
                    val newQuantity = (existingItem.quantity + delta).coerceAtLeast(1)
                    existingItem.copy(quantity = newQuantity)
                } else {
                    existingItem
                }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    val totalPrice: Double
        get() = _cartItems.value.sumOf { item ->
            // Assuming price string is like "₹60" or "60"
            val priceValue = item.price.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
            priceValue * item.quantity
        }
}
