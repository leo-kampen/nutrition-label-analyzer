package com.example.nutritionlabelapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaService {
    /**
     * Fire-and-forget multimodal generation.
     * Sends prompt + optional images, returns a single response string.
     */
    @POST("api/generate")
    suspend fun generate(
        @Body req: GenerateRequest
    ): Response<GenerateResponse>
}
