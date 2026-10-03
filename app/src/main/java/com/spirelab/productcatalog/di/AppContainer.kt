package com.spirelab.productcatalog.di

import android.content.Context
import com.spirelab.productcatalog.data.local.CartDatabase
import com.spirelab.productcatalog.data.remote.NetworkModule
import com.spirelab.productcatalog.data.repository.CartRepository
import com.spirelab.productcatalog.data.repository.ProductRepository

class AppContainer(context: Context) {
    private val api = NetworkModule.provideApi()
    private val db = CartDatabase.get(context)

    val productRepository = ProductRepository(api)
    val cartRepository = CartRepository(db.cartDao())
    val appContext: Context = context.applicationContext
}
