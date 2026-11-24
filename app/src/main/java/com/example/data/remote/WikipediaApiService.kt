package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WikiThumbnail(
    val source: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class WikiSummaryResponse(
    val title: String? = null,
    val description: String? = null,
    val extract: String? = null,
    val thumbnail: WikiThumbnail? = null,
    val originalimage: WikiThumbnail? = null
)

@JsonClass(generateAdapter = true)
data class WikiSearchPage(
    val pageid: Long? = null,
    val title: String? = null,
    val extract: String? = null,
    val thumbnail: WikiThumbnail? = null
)

@JsonClass(generateAdapter = true)
data class WikiSearchQuery(
    val pages: Map<String, WikiSearchPage>? = null
)

@JsonClass(generateAdapter = true)
data class WikiSearchResponse(
    val query: WikiSearchQuery? = null
)

interface WikipediaApiService {
    @GET("api/rest_v1/page/summary/{title}")
    suspend fun getPageSummary(
        @Path("title") title: String
    ): WikiSummaryResponse

    @GET("w/api.php")
    suspend fun searchBotanicalPages(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("generator") generator: String = "search",
        @Query("gsrsearch") query: String,
        @Query("gsrlimit") limit: Int = 10,
        @Query("prop") prop: String = "pageimages|extracts",
        @Query("exintro") exintro: Int = 1,
        @Query("explaintext") explaintext: Int = 1,
        @Query("exsentences") exsentences: Int = 3,
        @Query("pithumbsize") thumbSize: Int = 600
    ): WikiSearchResponse
}
