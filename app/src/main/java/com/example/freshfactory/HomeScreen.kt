package com.example.freshfactory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.freshfactory.BottomAppBar.CustomBottomAppBar
import com.example.freshfactory.CartScreen.CartContent
import com.example.freshfactory.CartScreen.CartItem
import com.example.freshfactory.CartScreen.CartScreen
import com.example.freshfactory.CartScreen.CartViewModel
import com.example.freshfactory.ui.theme.FreshFactoryTheme
import kotlinx.coroutines.launch
import com.example.freshfactory.ProfileScreen.ProfileContent
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds


@Serializable
data object HomeRoute : NavKey

@Serializable
data object CartRoute : NavKey

@Serializable
data object ProfileRoute : NavKey
@OptIn(ExperimentalMaterial3Api::class)


    @Composable
    fun AppNavigation(cartViewModel: CartViewModel = viewModel()) {
        val backStack = rememberNavBackStack(HomeRoute)
        val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
        
        // Sync selectedTab with the backstack state
        val selectedTab = when (backStack.lastOrNull()) {
            is HomeRoute -> 0
            is CartRoute -> 1
            is ProfileRoute -> 2
            else -> 0
        }
        
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        fun showSnackbar(message: String) {
            scope.launch {
                val snackbarJob = launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Indefinite
                    )
                }
                    delay(3000.milliseconds)
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarJob.cancel()
                }
            }


        Scaffold(
            containerColor = FreshFactoryBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val title = when (selectedTab) {
                            0 -> stringResource(R.string.fresh_factory)
                            1 -> stringResource(R.string.cart)
                            2 -> stringResource(R.string.profile)
                            else -> stringResource(R.string.fresh_factory)
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        if (selectedTab == 0) {
                            IconButton(onClick = { /* show menu list */ }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        } else if (backStack.size > 1) {
                            IconButton(onClick = { backStack.removeLastOrNull() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = { backStack.add(CartRoute) }) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                            }
                        } else {
                            // Spacer to balance the navigation icon and keep the title centered
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF2E7D32),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                CustomBottomAppBar(
                    selectedItem = selectedTab,
                    onItemSelected = { index ->
                        val route = when (index) {
                            0 -> HomeRoute
                            1 -> CartRoute
                            2 -> ProfileRoute
                            else -> HomeRoute
                        }
                        if (backStack.lastOrNull() != route) {
                            backStack.add(route)
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        )
        { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeRoute> {
                        HomeContent(
                            onAddToCart = {
                                cartViewModel.addToCart(it)
                                showSnackbar("${it.name} added to Cart")
                                          },
                            onBuyNow = {
                                cartViewModel.addToCart(it)
                                backStack.add(CartRoute)
                            }
                        )
                    }
                    entry<CartRoute> {
                        CartScreen (
                            cartItems = cartItems,
                            totalPrice = cartViewModel.totalPrice,
                            onIncreaseQuantity = { cartViewModel.updateQuantity(it, 1) },
                            onDecreaseQuantity = { cartViewModel.updateQuantity(it, -1) },
                            onRemoveItem = { cartViewModel.removeFromCart(it) })
                    }
                    entry<ProfileRoute> {
                        ProfileContent()
                    }
                }
            )
        }
    }

val FreshFactoryBackground = Color(0xFFF7FAF5)
@Composable
fun HomeContent(
    onAddToCart: (CartItem) -> Unit,
    onBuyNow: (CartItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = FreshFactoryBackground),
        contentPadding = PaddingValues(
            top = 10.dp,
            bottom = 10.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly
            ) {
                Milk(onAddToCart, onBuyNow)
                Ghee(onAddToCart, onBuyNow)
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly
            ) {
                Dahi(onAddToCart, onBuyNow)
                Paneer(onAddToCart, onBuyNow)
            }
        }
    }
}

@Composable
fun Milk(onAddToCart: (CartItem) -> Unit, onBuyNow: (CartItem) -> Unit) {
    val itemName = stringResource(R.string.milk_1l)
    val itemPrice = stringResource(R.string.price_60)
    val originalPrice = stringResource(R.string.price_66)
    val imageRes = R.drawable.milk_bottle_icon

    val cartItem = CartItem(1, itemName, itemPrice, originalPrice, imageRes)

    Card(
        onClick = { /* Go to Description page */ },
        modifier = Modifier
            .padding(start = 20.dp, top = 20.dp)
            .size(width = 150.dp, height = 300.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(135.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.padding(5.dp)) {
                    Image(
                        painter = painterResource(R.drawable.milk_bottle_icon),
                        contentDescription = stringResource(R.string.milk_bottle_image)
                    )
                    IconButton(
                        onClick = { onAddToCart(cartItem) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 3.dp, vertical = 4.dp)
                            .clip(RectangleShape)
                            .size(35.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF075E46),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_to_cart),
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price_60),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF116910),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text(
                    text = stringResource(R.string.price_66),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.Black,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.off_6),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.milk_1l),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddToCart(cartItem) }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_shopping_cart_24),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.padding(start = 10.dp))
                FilledTonalButton(
                    onClick = { onBuyNow(cartItem) },
                    shape = AbsoluteRoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(35.dp)

                ) {
                    Text(
                        text = stringResource(R.string.buy_now),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Ghee(onAddToCart: (CartItem) -> Unit, onBuyNow: (CartItem) -> Unit){
    val itemName = stringResource(R.string.ghee_1l)
    val itemPrice = stringResource(R.string.price_1100)
    val originalPrice = stringResource(R.string.price_1200)
    val imageRes = R.drawable.ghee_image_icon

    val cartItem = CartItem(2, itemName, itemPrice, originalPrice, imageRes)
    
    Card(
        onClick = { /* Go to Description page */ },
        modifier = Modifier.padding(start = 20.dp, top = 20.dp)
            .size(width = 150.dp, height = 300.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.size(135.dp),
                shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.padding(5.dp).size(140.dp)){
                    Image(
                        painter = painterResource(R.drawable.ghee_image_icon),
                        contentDescription = stringResource(R.string.milk_bottle_image),
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onAddToCart(cartItem) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 3.dp, vertical = 4.dp)
                            .clip(RectangleShape)
                            .size(35.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF075E46),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_to_cart),
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price_1100),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF116910),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text(
                    text = stringResource(R.string.price_1200),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.Black,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.off_100),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.ghee_1l),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddToCart(cartItem) }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_shopping_cart_24),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.padding(start = 10.dp))
                FilledTonalButton(
                    onClick = { onBuyNow(cartItem) },
                    shape = AbsoluteRoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(35.dp)

                ) {
                    Text(
                        text = stringResource(R.string.buy_now),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Dahi(onAddToCart: (CartItem) -> Unit, onBuyNow: (CartItem) -> Unit){
    val itemName = stringResource(R.string.dahi_1kg)
    val itemPrice = stringResource(R.string.price_90)
    val originalPrice = stringResource(R.string.price_95)
    val imageRes = R.drawable.dahi_image_icon

    val cartItem = CartItem(3, itemName, itemPrice, originalPrice, imageRes)
    
    Card(
        onClick = { /* Go to Description page */ },
        modifier = Modifier.padding(start = 20.dp, top = 20.dp)
            .size(width = 150.dp, height = 300.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.size(135.dp),
                shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.padding(5.dp).size(140.dp)){
                    Image(
                        painter = painterResource(R.drawable.dahi_image_icon),
                        contentDescription = stringResource(R.string.dahi_image),
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onAddToCart(cartItem) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 3.dp, vertical = 4.dp)
                            .clip(RectangleShape)
                            .size(35.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF075E46),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_to_cart),
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price_90),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF116910),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text(
                    text = stringResource(R.string.price_95),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.Black,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.off_5),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.dahi_1kg),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddToCart(cartItem) }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_shopping_cart_24),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.padding(start = 10.dp))
                FilledTonalButton(
                    onClick = { onBuyNow(cartItem) },
                    shape = AbsoluteRoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(35.dp)

                ) {
                    Text(
                        text = stringResource(R.string.buy_now),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Paneer(onAddToCart: (CartItem) -> Unit, onBuyNow: (CartItem) -> Unit){
    val itemName = stringResource(R.string.paneer_1kg)
    val itemPrice = stringResource(R.string.price_400)
    val originalPrice = stringResource(R.string.price_425)
    val imageRes = R.drawable.paneer_image_icon

    val cartItem = CartItem(4, itemName, itemPrice, originalPrice, imageRes)
    
    Card(
        onClick = { /* Go to Description page */ },
        modifier = Modifier.padding(start = 20.dp, top = 20.dp)
            .size(width = 150.dp, height = 300.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.size(135.dp),
                shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.padding(5.dp).size(140.dp)){
                    Image(
                        painter = painterResource(R.drawable.paneer_image_icon),
                        contentDescription = stringResource(R.string.paneer_image),
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onAddToCart(cartItem) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(horizontal = 3.dp, vertical = 4.dp)
                            .clip(RectangleShape)
                            .size(35.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF075E46),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_to_cart),
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price_400),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF116910),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text(
                    text = stringResource(R.string.price_425),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.Black,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.off_25),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.paneer_1kg),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddToCart(cartItem) }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_shopping_cart_24),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.padding(start = 10.dp))
                FilledTonalButton(
                    onClick = { onBuyNow(cartItem) },
                    shape = AbsoluteRoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(35.dp)

                ) {
                    Text(
                        text = stringResource(R.string.buy_now),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 1.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    FreshFactoryTheme {
        AppNavigation()
    }
}
