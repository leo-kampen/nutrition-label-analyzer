package com.example.nutritionlabelapp.network

/**
 * Payload for calling Ollama’s /api/generate endpoint.
 *
 * @param model The model identifier (e.g. "gemma3:4b").
 * @param prompt The text prompt to guide generation.
 * @param images Optional list of base64-encoded images.
 */
data class GenerateRequest(
    val model: String,
    val prompt: String,
    val images: List<String>? = null,
    val stream: Boolean = false
)

/**
 * Response from /api/generate.
 *
 * @param model      The model that generated this response.
 * @param created_at Timestamp string.
 * @param response   The generated text.
 */
data class GenerateResponse(
    val model: String,
    val created_at: String,
    val response: String
)
