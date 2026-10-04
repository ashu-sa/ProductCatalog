package com.spirelab.productcatalog.ui.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spirelab.productcatalog.data.model.Product
import com.spirelab.productcatalog.data.repository.CartRepository
import com.spirelab.productcatalog.data.repository.ProductRepository
import com.spirelab.productcatalog.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val error: String? = null,
    val addedToCart: Boolean = false
)

class ProductDetailViewModel(
    private val productId: Int,
    private val products: ProductRepository,
    private val cart: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState(isLoading = true)
            when (val r = products.getProduct(productId)) {
                is AppResult.Success -> _uiState.value = ProductDetailUiState(isLoading = false, product = r.data)
                is AppResult.Error -> _uiState.value = ProductDetailUiState(isLoading = false, error = r.message)
            }
        }
    }

    fun addToCart(quantity: Int = 1) {
        val p = _uiState.value.product ?: return
        viewModelScope.launch {
            cart.add(p, quantity)
            _uiState.value = _uiState.value.copy(addedToCart = true)
        }
    }

    fun consumeAddedFlag() {
        _uiState.value = _uiState.value.copy(addedToCart = false)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val productId: Int,
        private val products: ProductRepository,
        private val cart: CartRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductDetailViewModel(productId, products, cart) as T
        }
    }
}
