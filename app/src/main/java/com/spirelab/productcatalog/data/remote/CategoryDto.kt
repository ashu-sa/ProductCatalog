package com.spirelab.productcatalog.data.remote

// DummyJSON returns either ["beauty", ...] (strings) or [{slug, name, url}].
// Custom deserializer handles both via a Gson JsonDeserializer registered in Network module.
data class CategoryDto(
    val slug: String,
    val name: String
) {
    companion object {
        fun fromRaw(raw: String): CategoryDto {
            val slug = raw.lowercase().replace(" ", "-")
            val name = raw.replaceFirstChar { it.uppercase() }
            return CategoryDto(slug, name)
        }
    }
}
