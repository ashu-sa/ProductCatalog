package com.spirelab.productcatalog.util

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val isNetwork: Boolean = false) : AppResult<Nothing>
}
