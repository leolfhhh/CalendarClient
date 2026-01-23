package com.example.calendarclient.api

import android.util.Log
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
    private const val BASE_URL = "http://10.0.2.2:8000/" // Using 10.0.2.2 to access host localhost from emulator

    val api: AIAgentApi by lazy {
        // #region agent log
        Log.d("DEBUG_AI", "[H3] Retrofit instance created | baseUrl=$BASE_URL | fullEndpoint=${BASE_URL}process")
        // #endregion
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AIAgentApi::class.java)
    }
}
