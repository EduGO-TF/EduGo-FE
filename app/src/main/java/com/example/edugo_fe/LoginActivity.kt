package com.example.edugo_fe

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugo_fe.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val loginButton = binding.loginButton
        loginButton.setOnClickListener{
            loginWithKakao()
        }
    }

    private fun loginWithKakao(){
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            // 카톡을 통한 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleLoginResult(token, error)
            }
        } else {
            // 카카오 계정을 웹을 통한 로그인
            UserApiClient.instance.loginWithKakaoAccount(this){ token, error ->
                handleLoginResult(token, error)
            }
        }
    }

    private fun handleLoginResult(token: OAuthToken?, error: Throwable?){
        if (error != null){
            Log.e("KakaoLogin", "로그인 실패", error)
        } else if (token != null){
            // 성공적으로 인가 코드 반환
            Log.d("KakaoLogin", "로그인 성공: ${token.accessToken}")

            // 인가 코드를 백엔드로 전달
            sendAuthorizationCodeToBackend(token.accessToken)
        }
    }

    private fun sendAuthorizationCodeToBackend(authCode: String){
        // 백엔드로 인가 코드 전송
        val backendUrl = "https://your-backend.com/api/kakao/login"
        val requestBody = mapOf("auth code" to authCode)

        // Retrofit 또는 OkHttp를 사용해 POST 요청

    }
}