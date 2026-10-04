package com.spirelab.productcatalog.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spirelab.productcatalog.data.local.CartItemEntity
import com.spirelab.productcatalog.data.repository.CartRepository
import com.spirelab.productcatalog.data.repository.CartSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    val items: StateFlow<List<CartItemEntity>> =
        repository.observeItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<CartSummary> =
        items.map { CartRepository.summarize(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary(0, 0.0))

    fun increase(item: CartItemEntity) {
        if (item.quantity < item.stock) {
            viewModelScope.launch { repository.setQuantity(item.productId, item.quantity + 1) }
        }
    }

    fun decrease(item: CartItemEntity) {
        viewModelScope.launch { repository.setQuantity(item.productId, item.quantity - 1) }
    }

    fun remove(item: CartItemEntity) {
        viewModelScope.launch { repository.remove(item.productId) }
    }

    fun clear() {
        viewModelScope.launch { repository.clear() }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repo: CartRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(repo) as T
        }
    }
}
