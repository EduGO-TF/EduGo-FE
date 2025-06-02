package com.example.edugo_fe.network

import com.example.edugo_fe.ApiData.DetectionsResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthService {
    @FormUrlEncoded
    @POST("auth/refresh")
    fun refreshToken(
        @Field("refresh_token") refreshToken: String
    ): Call<TokenResponse>

    @Multipart
    @POST("/detect")
    fun getForest(@Part image: MultipartBody.Part): Call<DetectionsResponse>
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long,
)

data class UserProfile(
    val name: String,
    val email: String,
)