package com.example.nutritionlabelapp.network

data class OllamaChatMessage(
    val role: String,
    val content: String
)

/**
 * The body you POST to /api/chat
 */
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaChatMessage>,
    val stream: Boolean = false
)

/**
 * Non‐streaming chat response from Ollama.
 * It has a single top‐level "message" field, not a "choices" array.
 */
data class OllamaChatResponse(
    val model: String,
    val created_at: String,       // optional, but usually present
    val message: OllamaChatMessage
)
