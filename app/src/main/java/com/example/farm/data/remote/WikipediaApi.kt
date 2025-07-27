package com.example.farm.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response

data class WikipediaSummaryResponse(
    val title: String?,
    val extract: String?,
    val description: String?,
    val thumbnail: Thumbnail?
)

data class Thumbnail(
    val source: String?,
    val width: Int?,
    val height: Int?
)

interface WikipediaApi {
    @GET("page/summary/{title}")
    suspend fun getSummary(@Path("title") title: String): Response<WikipediaSummaryResponse>
} 