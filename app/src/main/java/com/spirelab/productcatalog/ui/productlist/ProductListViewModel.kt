package com.spirelab.productcatalog.ui.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spirelab.productcatalog.data.model.Product
import com.spirelab.productcatalog.data.repository.ProductRepository
import com.spirelab.productcatalog.util.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductListUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null
)

class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allProducts: List<Product> = emptyList()
    private var searchResults: List<Product> = emptyList()

    init {
        refresh()
        loadCategories()
    }

    fun refresh() {
        val q = _uiState.value.query
        if (q.isBlank()) {
            loadProducts()
        } else {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                executeSearch(q)
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            when (val r = repository.getCategories()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        categories = r.data.map { it.name }.distinct().sorted()
                    )
                }
                is AppResult.Error -> { /* non-fatal: categories optional */ }
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val r = repository.getProducts(limit = 60)) {
                is AppResult.Success -> {
                    allProducts = r.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = applyCategoryFilter(r.data),
                        error = null
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = r.message,
                        isOffline = r.isNetwork
                    )
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery, isSearching = newQuery.isNotBlank())
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            searchResults = emptyList()
            _uiState.value = _uiState.value.copy(
                products = applyCategoryFilter(allProducts), error = null, isSearching = false
            )
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // debounce
            executeSearch(newQuery)
        }
    }

    private suspend fun executeSearch(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSearching = true)
        when (val r = repository.searchProducts(query)) {
            is AppResult.Success -> {
                searchResults = r.data
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = applyCategoryFilter(searchResults),
                    error = null
                )
            }
            is AppResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = r.message, isOffline = r.isNetwork
                )
            }
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        val base = if (_uiState.value.query.isBlank()) allProducts else searchResults
        _uiState.value = _uiState.value.copy(products = applyCategoryFilter(base))
    }

    private fun applyCategoryFilter(list: List<Product>): List<Product> {
        val sel = _uiState.value.selectedCategory ?: return list
        return list.filter { it.category.equals(sel, ignoreCase = true) }
    }

    fun setOfflineBanner(isOffline: Boolean) {
        _uiState.value = _uiState.value.copy(isOffline = isOffline)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repo: ProductRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductListViewModel(repo) as T
        }
    }
}
