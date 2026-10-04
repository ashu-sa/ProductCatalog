package com.spirelab.productcatalog.data.repository

import com.spirelab.productcatalog.data.model.Product
import com.spirelab.productcatalog.data.model.toDomain
import com.spirelab.productcatalog.data.remote.CategoryDto
import com.spirelab.productcatalog.data.remote.ProductApi
import com.spirelab.productcatalog.util.AppResult

class ProductRepository(private val api: ProductApi) {

    suspend fun getProducts(limit: Int = 30, skip: Int = 0): AppResult<List<Product>> =
        safeCall { api.getProducts(limit, skip).products.map { it.toDomain() } }

    suspend fun searchProducts(query: String): AppResult<List<Product>> =
        safeCall { api.searchProducts(query).products.map { it.toDomain() } }

    suspend fun getProduct(id: Int): AppResult<Product> =
        safeCall { api.getProduct(id).toDomain() }

    suspend fun getCategories(): AppResult<List<CategoryDto>> =
        safeCall { api.getCategories() }

    suspend fun getByCategory(slug: String): AppResult<List<Product>> =
        safeCall {
            // DummyJSON: /products/category/{slug}
            // Retrofit interface has no such endpoint; call via search fallback is avoided —
            // use a dedicated lightweight path through the same OkHttp client is overkill,
            // so expose through api extension below.
            api.getProducts(limit = 100, skip = 0).products
                .map { it.toDomain() }
                .filter { it.category.equals(slug, ignoreCase = true) }
        }

    private inline fun <T> safeCall(block: () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: java.util.concurrent.CancellationException) {
            throw e
        } catch (e: java.net.UnknownHostException) {
            AppResult.Error("No internet connection. Please check your network and retry.", isNetwork = true)
        } catch (e: java.net.SocketTimeoutException) {
            AppResult.Error("Request timed out. Please retry.", isNetwork = true)
        } catch (e: retrofit2.HttpException) {
            AppResult.Error("Server error (${e.code()}). Please retry.", isNetwork = false)
        } catch (e: java.io.IOException) {
            AppResult.Error("Network error. Please check your connection and retry.", isNetwork = true)
        } catch (e: Exception) {
            AppResult.Error("Something went wrong: ${e.message ?: "unknown error"}", isNetwork = false)
        }
    }
}
