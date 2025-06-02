package com.example.edugo_fe.Login

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.ActivityLoginBinding
import com.example.edugo_fe.network.ApiClient
import com.example.edugo_fe.network.AuthFailureListener
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity(), AuthFailureListener {
    private lateinit var binding :ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ApiClient.setAuthFailureListener(this)

    }

    override fun onResume() {
        super.onResume()
        checkTokenAndNavigate()
    }

    private fun checkTokenAndNavigate() {
        val accessToken = SecurePrefs.getAccessToken(this)
        if (accessToken != null) {
            showOnBoardingFragment()
        } else {
            showOnBoardingFragment()
//            showLoginMainFragment()
        }
    }

    private fun showLoginMainFragment() {
        supportFragmentManager.commit {
            replace(R.id.frame_layout, LoginMainFragment())
            setReorderingAllowed(true)
        }
    }

    fun showOnBoardingFragment() {
        supportFragmentManager.commit {
            replace(R.id.frame_layout, OnBoardingFragment())
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    override fun onAuthFailure() {
        supportFragmentManager.commit {
            replace(R.id.frame_layout, LoginMainFragment())
            setReorderingAllowed(true)
        }
    }

}