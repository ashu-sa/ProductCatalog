package com.spirelab.productcatalog.data.remote

import com.google.gson.annotations.SerializedName

data class ProductsResponse(
    val products: List<ProductDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    @SerializedName("discountPercentage") val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val brand: String? = null,
    val category: String = "",
    val thumbnail: String = "",
    val images: List<String> = emptyList()
)
