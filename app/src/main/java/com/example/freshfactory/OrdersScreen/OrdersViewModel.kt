package com.example.freshfactory.OrdersScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshfactory.DataBase.AppDatabase
import com.example.freshfactory.DataBase.OrderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data class Success(
        val orders: List<OrderEntity>,
        val selectedFilter: String? = null
    ) : OrdersUiState
}

class OrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val orderDao = AppDatabase.getDatabase(application).orderDao

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter.asStateFlow()

    val uiState: StateFlow<OrdersUiState> = combine(
        orderDao.getAllOrders(),
        _selectedFilter
    ) { orders, filter ->
        val filteredList = if (filter.isNullOrEmpty() || filter.equals("All", ignoreCase = true)) {
            orders
        } else {
            orders.filter { it.status.equals(filter, ignoreCase = true) }
        }
        OrdersUiState.Success(orders = filteredList, selectedFilter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OrdersUiState.Loading
    )

    init {
        seedInitialMockOrdersIfEmpty()
    }

    private fun seedInitialMockOrdersIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val existingOrders = orderDao.getAllOrders().firstOrNull()
            if (existingOrders.isNullOrEmpty()) {
                val mockOrders = listOf(
                    OrderEntity(
                        id = "ORD-98421",
                        orderDate = "24 Oct 2024, 08:30 AM",
                        totalAmount = 180.0,
                        status = "Delivered",
                        deliveryAddress = "123 Green Avenue, Flat 4B, City",
                        paymentMethod = "UPI / PhonePe",
                        itemsCount = 3,
                        itemsSummary = "2x Fresh Cow Milk 1L, 1x Fresh Paneer 200g"
                    ),
                    OrderEntity(
                        id = "ORD-98422",
                        orderDate = "25 Oct 2024, 07:15 AM",
                        totalAmount = 240.0,
                        status = "Out for Delivery",
                        deliveryAddress = "123 Green Avenue, Flat 4B, City",
                        paymentMethod = "Cash on Delivery",
                        itemsCount = 2,
                        itemsSummary = "1x Cow Ghee 500ml, 1x Buffalo Milk 1L"
                    ),
                    OrderEntity(
                        id = "ORD-98420",
                        orderDate = "20 Oct 2024, 09:00 AM",
                        totalAmount = 90.0,
                        status = "Cancelled",
                        deliveryAddress = "123 Green Avenue, Flat 4B, City",
                        paymentMethod = "UPI",
                        itemsCount = 1,
                        itemsSummary = "1x Fresh Curd 500g"
                    )
                )
                orderDao.insertOrders(mockOrders)
            }
        }
    }

    fun setFilter(filter: String?) {
        _selectedFilter.value = filter
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            orderDao.updateOrderStatus(orderId, "Cancelled")
        }
    }

    fun reorder(order: OrderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val newOrderId = "ORD-${System.currentTimeMillis().toString().takeLast(5)}"
            val newOrder = order.copy(
                id = newOrderId,
                orderDate = "Just now",
                status = "Placed"
            )
            orderDao.insertOrder(newOrder)
        }
    }
}
