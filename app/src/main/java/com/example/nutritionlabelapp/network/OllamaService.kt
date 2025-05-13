package com.example.nutritionlabelapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaService {
    /**
     * Chat-style endpoint: sends all messages so far, returns assistant’s reply.
     */
    @POST("api/chat")
    suspend fun chat(
        @Body req: OllamaChatRequest
    ): Response<OllamaChatResponse>
}
