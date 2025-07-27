package com.example.farm.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

data class OpenAIChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<OpenAIMessage>,
    val max_tokens: Int = 512,
    val temperature: Double = 0.7
)

data class OpenAIMessage(
    val role: String = "user",
    val content: String
)

data class OpenAIChatResponse(
    val choices: List<OpenAIChoice>
)

data class OpenAIChoice(
    val message: OpenAIMessage
)

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: OpenAIChatRequest): Response<OpenAIChatResponse>
} 