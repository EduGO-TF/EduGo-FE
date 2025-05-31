package com.example.edugo_fe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.Login.KeystoreHelper
import com.example.edugo_fe.Login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class SplashActivity : AppCompatActivity() {
    private lateinit var keystoreHelper: KeystoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        keystoreHelper = KeystoreHelper(this)

        enableEdgeToEdge()

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val accessToken = prefs.getString("accessToken", null)

        Log.d("AIResponse", "AccessToken : $accessToken")

        lifecycleScope.launch {
            delay(1500)
            checkLogin(accessToken)
        }


    }

    private fun checkToken() {
        val accessToken = SecurePrefs.getAccessToken(this)
        val refreshToken = keystoreHelper.decryptToken()

        when {
            accessToken != null -> navigateToMain()
            refreshToken != null -> refreshAccessToken(refreshToken)
            else -> navigateToLogin()
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        Retrofit.Builder()

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