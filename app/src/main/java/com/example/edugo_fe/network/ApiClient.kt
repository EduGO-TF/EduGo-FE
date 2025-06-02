package com.example.edugo_fe.network

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.edugo_fe.Login.KeystoreHelper
import com.example.edugo_fe.Login.SecurePrefs
import com.example.edugo_fe.network.ApiClient.authFailureListener
import com.example.edugo_fe.network.ApiClient.context
import com.example.edugo_fe.network.ApiClient.createService
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.internal.http2.Http2Reader
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.Handler
import kotlin.math.log

// 인증 실패 콜백 인터페이스 정의
interface AuthFailureListener {
    fun onAuthFailure()
}

// 토큰 갱신 인증기
class TokenAuthenticator(
    private val context: Context,
    private val authFailureListener: AuthFailureListener?
): Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            val refreshToken = KeystoreHelper(context).decryptToken() ?: run {
                notifyAuthFailure() // 콜백 호출
                return null
            }
            val newToken = refreshAccessToken(refreshToken) ?: run {
                notifyAuthFailure() // 콜백 호출
                return null
            }

            SecurePrefs.saveAccessToken(context, newToken)
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun notifyAuthFailure() {
        // 메인 스레드 실행 보장
        android.os.Handler(Looper.getMainLooper()).post {
            authFailureListener?.onAuthFailure()
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        val call = createService(AuthService::class.java)
            .refreshToken(refreshToken)
        val response = call.execute()
        return response.body()?.accessToken
    }

}


@SuppressLint("StaticFieldLeak")
object ApiClient {

    private const val BASE_URL = "https://edugoapp.link/"
    private lateinit var context: Context
    private var authFailureListener: AuthFailureListener ?= null

    fun init(context: Context){
        this.context = context.applicationContext
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .authenticator(TokenAuthenticator(context, authFailureListener))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // 인증 헤더 추가 인터셉터
    private class AuthInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val accessToken = SecurePrefs.getAccessToken(context)

            return if (accessToken != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
    }

    fun setAuthFailureListener(listener: AuthFailureListener){
        this.authFailureListener = listener
    }
}

