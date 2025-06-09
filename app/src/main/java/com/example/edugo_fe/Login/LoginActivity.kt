package com.example.edugo_fe.Login

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.ActivityLoginBinding
import com.example.edugo_fe.network.ApiClient
import com.example.edugo_fe.network.AuthFailureListener
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity(), AuthFailureListener {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ApiClient.setAuthFailureListener(this)

//        initViewPager()

    }

    override fun onResume() {
        super.onResume()
        checkTokenAndNavigate()
    }

    private fun initViewPager() {
        viewPager = binding.viewPager
        val viewPagerAdapter = LoginAdapter(this)
        viewPager.adapter = viewPagerAdapter
    }

    private fun checkTokenAndNavigate() {
//        val accessToken = SecurePrefs.getAccessToken(this)
        // accessToken dummy값 만들어주기
        val accessToken = 1
        if (accessToken != null) {
            showOnBoardingFragment()
        } else {
//            showOnBoardingFragment()
            showLoginMainFragment()
        }
    }

    private fun showLoginMainFragment() {
//        supportFragmentManager.commit {
//            replace(R.id.view_pager, LoginMainFragment())
//            setReorderingAllowed(true)
//        }
        initViewPager()
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
            replace(R.id.view_pager, LoginMainFragment())
            setReorderingAllowed(true)
        }
    }

}