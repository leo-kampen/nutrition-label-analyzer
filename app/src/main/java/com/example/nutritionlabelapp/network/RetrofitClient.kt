package com.example.nutritionlabelapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Points at your local Ollama serve: 10.0.2.2:11434
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:11435/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val ollamaService: OllamaService =
        retrofit.create(OllamaService::class.java)
}
