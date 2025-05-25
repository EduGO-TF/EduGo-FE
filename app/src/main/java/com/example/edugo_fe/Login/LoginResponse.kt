package com.example.edugo_fe.Login

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val nickname: String,
)
