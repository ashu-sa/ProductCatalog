package com.spirelab.productcatalog.data.model

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val brand: String,
    val category: String,
    val thumbnail: String,
    val images: List<String>
)

fun com.spirelab.productcatalog.data.remote.ProductDto.toDomain(): Product =
    Product(
        id = id,
        title = title,
        description = description,
        price = price,
        rating = rating,
        stock = stock,
        brand = brand ?: "Unknown",
        category = category,
        thumbnail = thumbnail.ifBlank { images.firstOrNull().orEmpty() },
        images = images.ifEmpty { listOfNotNull(thumbnail.ifBlank { null }) }
    )
