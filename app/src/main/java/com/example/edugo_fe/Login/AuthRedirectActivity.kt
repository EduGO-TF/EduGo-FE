package com.example.edugo_fe.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthRedirectActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent?) {
        val uri = intent?.data
        val accessToken = uri?.getQueryParameter("accessToken")
        val refreshToken = uri?.getQueryParameter("refreshToken")

        if (accessToken != null && refreshToken != null) {
            // 1. RefreshToken 암호화 저장
            KeystoreHelper(this).encryptAndSaveToken(refreshToken)

            // 2. AccessToken은 메모리 전 (ViewModel 사용 권장)
            TokenManager.accessToken = accessToken

            // 3. 메인 화면 이동
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } else {
            Toast.makeText(this, "토큰 수신 실패", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

object TokenManager {
    var accessToken: String? = null
}