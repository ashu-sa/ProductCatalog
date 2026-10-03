package com.spirelab.productcatalog.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val categoryListDeserializer =
        JsonDeserializer<List<CategoryDto>> { json: JsonElement, _: Type, _: JsonDeserializationContext ->
            if (!json.isJsonArray) return@JsonDeserializer emptyList()
            json.asJsonArray.map { el ->
                if (el.isJsonPrimitive) {
                    CategoryDto.fromRaw(el.asString)
                } else {
                    val obj = el.asJsonObject
                    val slug = obj.get("slug")?.asString ?: ""
                    val name = obj.get("name")?.asString ?: slug
                    CategoryDto(slug, name)
                }
            }
        }

    fun provideApi(): ProductApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(
                object : com.google.gson.reflect.TypeToken<List<CategoryDto>>() {}.type,
                categoryListDeserializer
            )
            .create()

        // The categories endpoint returns a raw JSON array, so wrap it for Gson parsing.
        // We keep Retrofit's converter simple and parse categories manually instead.
        return Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ProductApi::class.java)
    }
}
