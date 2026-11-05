package com.example.freshfactory.OrdersScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshfactory.DataBase.OrderEntity
import com.example.freshfactory.FreshFactoryBackground
import com.example.freshfactory.ui.theme.FreshFactoryTheme
import java.util.Locale

@Composable
fun MyOrdersScreen(
    viewModel: OrdersViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is OrdersUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is OrdersUiState.Success -> {
            MyOrdersContent(
                orders = state.orders,
                selectedFilter = state.selectedFilter,
                onFilterSelected = { filter -> viewModel.setFilter(filter) },
                onCancelOrder = { orderId -> viewModel.cancelOrder(orderId) },
                onReorder = { order -> viewModel.reorder(order) },
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersContent(
    orders: List<OrderEntity>,
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit,
    onCancelOrder: (String) -> Unit,
    onReorder: (OrderEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val filterCategories = listOf("All", "Placed", "Out for Delivery", "Delivered", "Cancelled")

    Scaffold(
        containerColor = FreshFactoryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterCategories) { category ->
                    val isSelected = (selectedFilter == null && category == "All") ||
                            (selectedFilter.equals(category, ignoreCase = true))

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (category == "All") onFilterSelected(null)
                            else onFilterSelected(category)
                        },
                        label = { Text(text = category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (orders.isEmpty()) {
                EmptyOrdersState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCardItem(
                            order = order,
                            onCancelOrder = { onCancelOrder(order.id) },
                            onReorder = { onReorder(order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCardItem(
    order: OrderEntity,
    onCancelOrder: () -> Unit,
    onReorder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Order ID, Date, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.orderDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                OrderStatusBadge(status = order.status)
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Order Content Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Text(
                        text = "${order.itemsCount} Items • ${order.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Footer Row: Price & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                when (order.status.lowercase()) {
                    "placed", "out for delivery" -> {
                        OutlinedButton(
                            onClick = onCancelOrder,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel Order")
                        }
                    }
                    else -> {
                        Button(
                            onClick = onReorder,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-order")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val (backgroundColor, textColor, icon) = when (status.lowercase()) {
        "delivered" -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        "out for delivery" -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1565C0),
            Icons.Default.LocalShipping
        )
        "placed" -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFF57F17),
            Icons.Default.Schedule
        )
        else -> Triple( // Cancelled
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Default.Cancel
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EmptyOrdersState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Text(
                text = "No Orders Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "When you place orders, they will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyOrdersContentPreview() {
    FreshFactoryTheme {
        Surface {
            MyOrdersContent(
                orders = listOf(
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
                    )
                ),
                selectedFilter = "All",
                onFilterSelected = {},
                onCancelOrder = {},
                onReorder = {},
                onBackClick = {}
            )
        }
    }
}
