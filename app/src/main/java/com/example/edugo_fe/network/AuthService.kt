package com.example.edugo_fe.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthService {
    @FormUrlEncoded
    @POST("auth/refresh")
    fun refreshToken(
        @Field("refresh_token") refreshToken: String
    ): Call<TokenResponse>
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