package com.example.edugo_fe.Login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import com.example.edugo_fe.databinding.FragmentSecondLandingBinding

class SecondLandingFragment : Fragment() {
    private var _binding: FragmentSecondLandingBinding? = null
    private val binding get() = _binding!!

    // 딥링크 결과 처리용 런처
    private val authRedirectLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("AIResponse", "result : ${result.toString()}")
        if (result.resultCode == RESULT_OK) {
            (activity as? LoginActivity)?.showOnBoardingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondLandingBinding.inflate(inflater, container, false)

        // 카카오 CustomTab 이용
        binding.kakaoLoginButton.setOnClickListener {
            val backendKakaoLoginUrl = "http://edugoapp.link/oauth2/authorization/kakao"
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(backendKakaoLoginUrl))
            Log.d("AIResponse", "Clicked!")

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }

}