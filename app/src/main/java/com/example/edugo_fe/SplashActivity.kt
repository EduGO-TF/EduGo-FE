package com.example.edugo_fe

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.Login.KeystoreHelper
import com.example.edugo_fe.Login.LoginActivity
import com.example.edugo_fe.Login.SecurePrefs
import com.example.edugo_fe.network.ApiClient
import com.example.edugo_fe.network.AuthService
import com.example.edugo_fe.network.TokenResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    private lateinit var keystoreHelper: KeystoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        keystoreHelper = KeystoreHelper(this)

        lifecycleScope.launch {
            delay(1500)
            checkTokens()
        }
    }

    private fun checkTokens() {
        val accessToken = SecurePrefs.getAccessToken(this)
        val refreshToken = keystoreHelper.decryptToken()

        when {
            accessToken != null -> navigateToMain()
            refreshToken != null -> refreshAccessToken(refreshToken)
            else -> navigateToLogin()
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        ApiClient.createService(AuthService::class.java)
            .refreshToken(refreshToken)
            .enqueue(object : Callback<TokenResponse> {
                override fun onResponse(
                    call: Call<TokenResponse>,
                    response: Response<TokenResponse>
                ) {
                    if (response.isSuccessful) {
                        SecurePrefs.saveAccessToken(this@SplashActivity, response.body()!!.accessToken)
                        navigateToMain()
                    } else {
                        navigateToLogin()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    navigateToLogin()
                }

            })
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}