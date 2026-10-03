package com.spirelab.productcatalog.ui.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spirelab.productcatalog.ui.components.ErrorView
import com.spirelab.productcatalog.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.addedToCart) {
        if (state.addedToCart) {
            snackbar.showSnackbar("Added to cart")
            viewModel.consumeAddedFlag()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when {
            state.isLoading -> LoadingView("Loading product...")
            state.error != null -> ErrorView(state.error!!, onRetry = viewModel::load)
            state.product != null -> {
                val p = state.product!!
                Column(
                    Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = p.images.firstOrNull() ?: p.thumbnail,
                        contentDescription = p.title,
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(Modifier.padding(16.dp)) {
                        Text(p.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFA000))
                            Spacer(Modifier.width(4.dp))
                            Text(String.format("%.1f / 5", p.rating), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(12.dp))
                            Text("Stock: ${p.stock}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("$${p.price}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text(p.category) })
                            AssistChip(onClick = {}, label = { Text(p.brand) })
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(p.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.addToCart() },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }
}
