package com.spirelab.productcatalog.data.repository

import com.spirelab.productcatalog.data.local.CartDao
import com.spirelab.productcatalog.data.local.CartItemEntity
import com.spirelab.productcatalog.data.model.Product
import kotlinx.coroutines.flow.Flow

data class CartSummary(val itemCount: Int, val totalPrice: Double)

class CartRepository(private val dao: CartDao) {

    fun observeItems(): Flow<List<CartItemEntity>> = dao.observeItems()

    suspend fun add(product: Product, quantity: Int = 1) {
        val existing = dao.getById(product.id)
        val maxStock = if (product.stock > 0) product.stock else Int.MAX_VALUE
        val nextQty = ((existing?.quantity ?: 0) + quantity).coerceIn(1, maxStock)
        dao.upsert(
            CartItemEntity(
                productId = product.id,
                title = product.title,
                price = product.price,
                thumbnail = product.thumbnail,
                stock = product.stock,
                quantity = nextQty,
                addedAt = existing?.addedAt ?: System.currentTimeMillis()
            )
        )
    }

    suspend fun setQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            dao.deleteById(productId)
        } else {
            val existing = dao.getById(productId) ?: return
            val maxStock = if (existing.stock > 0) existing.stock else Int.MAX_VALUE
            dao.upsert(existing.copy(quantity = quantity.coerceAtMost(maxStock)))
        }
    }

    suspend fun remove(productId: Int) = dao.deleteById(productId)

    suspend fun clear() = dao.clear()

    companion object {
        fun summarize(items: List<CartItemEntity>): CartSummary {
            val count = items.sumOf { it.quantity }
            val total = items.sumOf { it.price * it.quantity }
            return CartSummary(count, total)
        }
    }
}
