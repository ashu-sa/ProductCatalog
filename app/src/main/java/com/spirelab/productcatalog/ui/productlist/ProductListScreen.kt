package com.spirelab.productcatalog.ui.productlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spirelab.productcatalog.data.model.Product
import com.spirelab.productcatalog.ui.components.EmptyView
import com.spirelab.productcatalog.ui.components.ErrorView
import com.spirelab.productcatalog.ui.components.LoadingView
import com.spirelab.productcatalog.ui.components.OfflineBanner
import com.spirelab.productcatalog.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    cartCount: Int,
    isOffline: Boolean,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Catalog") },
                actions = {
                    IconButton(onClick = onCartClick) {
                        BadgedBox(badge = { if (cartCount > 0) Badge { Text("$cartCount") } }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (isOffline) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    OfflineBanner()
                }
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            if (state.categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategory == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text("All") }
                        )
                    }
                    items(state.categories) { cat ->
                        FilterChip(
                            selected = state.selectedCategory == cat,
                            onClick = {
                                viewModel.onCategorySelected(
                                    if (state.selectedCategory == cat) null else cat
                                )
                            },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            when {
                state.isLoading -> LoadingView()
                state.error != null -> ErrorView(state.error!!, onRetry = viewModel::refresh)
                state.products.isEmpty() -> EmptyView(
                    if (state.query.isNotBlank()) "No products found for \"${state.query}\""
                    else "No products available."
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCard(product = product, onClick = { onProductClick(product) })
                    }
                }
            }
        }
    }
}
