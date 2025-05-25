package com.example.edugo_fe.Login

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edugo_fe.network.ApiService
import com.google.firebase.appdistribution.gradle.ApiService
import okhttp3.Callback

class AuthRedirectActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val code = intent?.data?.getQueryParameter("code")
        if (code != null) {
            sendCodeToBackend(code)
        } else {
            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun sendCodeToBackend(code: String) {
        // Retrofit 또는 OkHttp로 백엔드에 전송
        val api = ApiService.create()
        api.sendKakaoCode(mapOf("code" to code))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse()
            })
    }
}