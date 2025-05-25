package com.example.edugo_fe.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edugo_fe.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRedirectActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getTokens()
    }

    private fun getTokens() {

        // url에서 파라미터 추출
        val uri = intent?.data
        Log.d("AuthRedirectActivity", "URI: $uri") // URI 값을 로그로 출력
        val accessToken = uri?.getQueryParameter("accessToken")
        val refreshToken = uri?.getQueryParameter("refreshToken")


        if (accessToken != null && refreshToken != null) {
            // sharedPreferences에 저장
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            prefs.edit().apply {
                putString("accessToken", accessToken)
                putString("refreshToken", refreshToken)
                apply()
            }

            // MainActivity로 이동
            val intent = Intent(this@AuthRedirectActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "토큰 수신 실패", Toast.LENGTH_SHORT).show()
            finish()
        }

        Log.d("Activityarar", uri.toString())
    }

}