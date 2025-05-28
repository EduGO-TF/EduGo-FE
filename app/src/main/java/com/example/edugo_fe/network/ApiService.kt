package com.example.edugo_fe.network

import com.example.edugo_fe.ApiData.DetectionsResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST ("/detect")
    fun getForest(
        @Part image: MultipartBody.Part,
    ): Call<DetectionsResponse>
}