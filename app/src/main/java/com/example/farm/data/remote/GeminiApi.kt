package com.example.farm.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response

// Gemini API request/response models

data class GeminiContentPart(val text: String)
data class GeminiContent(val parts: List<GeminiContentPart>)
data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiCandidateContentPart(val text: String)
data class GeminiCandidateContent(val parts: List<GeminiCandidateContentPart>)
data class GeminiCandidate(val content: GeminiCandidateContent)
data class GeminiResponse(val candidates: List<GeminiCandidate>)

interface GeminiApi {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
} 