package com.example.edugo_fe.Login

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentLoginMainBinding

class LoginMainFragment : Fragment() {
    private var _binding : FragmentLoginMainBinding ?= null
    private val binding get() = _binding!!  // 안전하게 접근할 수 있도록 설정

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 여기서 뷰와 상호작용
        binding.kakaoLoginButton.setOnClickListener {
            val kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?" +
                    "client_id=" +
                    "&redirect_uri=myapp://ouath" +
                    "&response_type=code"
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(this, Uri.parse(kakaoAuthUrl))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }

}