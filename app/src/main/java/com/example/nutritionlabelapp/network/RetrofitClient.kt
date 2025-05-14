// app/src/main/java/com/example/nutritionlabelapp/network/RetrofitClient.kt
package com.example.nutritionlabelapp.network

import com.google.gson.GsonBuilder
import com.google.android.datatransport.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BASIC
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    // Create a Gson instance that’s lenient
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:11434/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))  // ← use lenient GSON
        .build()

    val ollamaService: OllamaService =
        retrofit.create(OllamaService::class.java)
}
