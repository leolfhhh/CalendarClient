package com.example.calendarclient.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class AIRequest(val query: String)

data class AIResponse(
    val summary: String,
    val description: String?,
    val location: String?,
    @SerializedName("dt_start") val dtStart: String, // ISO format e.g. 2026-01-22T14:00:00
    @SerializedName("dt_end") val dtEnd: String      // ISO format
)

interface AIAgentApi {
    @POST("process")
    suspend fun processQuery(@Body request: AIRequest): AIResponse
}

object AIAgentService {
    private const val BASE_URL = "http://your-ai-backend-url.com/" // Placeholder

    val api: AIAgentApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AIAgentApi::class.java)
    }
}
