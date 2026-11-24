package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OffNutriments(
    @Json(name = "sugars_100g") val sugars100g: Double? = null
)

@JsonClass(generateAdapter = true)
data class OffProduct(
    val code: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "image_front_url") val imageFrontUrl: String? = null,
    @Json(name = "image_small_url") val imageSmallUrl: String? = null,
    val categories: String? = null,
    val nutriments: OffNutriments? = null
)

@JsonClass(generateAdapter = true)
data class OffSearchResponse(
    val products: List<OffProduct>? = null
)

interface OpenFoodFactsApiService {
    @GET("cgi/search.pl")
    suspend fun searchFoods(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 10,
        @Query("fields") fields: String = "code,product_name,image_front_url,image_small_url,categories,nutriments"
    ): OffSearchResponse
}
