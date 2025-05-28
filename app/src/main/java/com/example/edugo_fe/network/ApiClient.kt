package com.example.edugo_fe.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

object ApiClient {

    private const val BASE_URL = "https://edugoapp.link/"

    val retrofit: Retrofit by lazy {
        // OkHttp 로깅 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // ApiService를 생성하는 함수
    fun createApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}