package com.example.edugo_fe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.Login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val accessToken = prefs.getString("accessToken", null)

        Log.d("AIResponse", "AccessToken : $accessToken")

        lifecycleScope.launch {
            delay(1500)
            checkLogin(accessToken)
        }


    }

    private fun checkLogin(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            // 로그인 화면으로 이동
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // 메인 화면 이동
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
//            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}