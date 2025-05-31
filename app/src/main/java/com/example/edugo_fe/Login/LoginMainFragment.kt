package com.example.edugo_fe.Login

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.ArActivity
import com.example.edugo_fe.MainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentLoginMainBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

//class LoginMainFragment : Fragment() {
//    private var _binding : FragmentLoginMainBinding ?= null
//    private val binding get() = _binding!!  // 안전하게 접근할 수 있도록 설정
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        _binding = FragmentLoginMainBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//
//        val prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE)
//        val accessToken = prefs.getString("accessToken", null)
//
//        Log.d("LoginMainFragment", "Access token: $accessToken") // 로그 추가
//
//        if (accessToken != null) {
//            binding.homeButton.visibility = View.VISIBLE
//            binding.kakaoLoginButton.visibility = View.INVISIBLE
//            binding.cameraButton.visibility = View.VISIBLE
//            binding.edugoStartMent.visibility = View.VISIBLE
//            binding.startMent.text = "숲을 찾아보아요"
//        } else {
//            binding.homeButton.visibility = View.INVISIBLE
//            binding.kakaoLoginButton.visibility = View.VISIBLE
//            binding.cameraButton.visibility = View.INVISIBLE
//            binding.edugoStartMent.visibility = View.INVISIBLE
//            binding.startMent.text = "이야기 속으로\n들어가 보아요"
//        }
//
//        // 여기서 뷰와 상호작용
//        binding.kakaoLoginButton.setOnClickListener {
//            val backendKakaoLoginUrl = "http://edugoapp.link/oauth2/authorization/kakao"
//            val customTabsIntent = CustomTabsIntent.Builder().build()
//            customTabsIntent.launchUrl(requireContext(), Uri.parse(backendKakaoLoginUrl))
//            Log.d("AIResponse", "Clicked!")
//        }
//
//        // 카메라 버튼 누를 경우 ArActivity활성화
//        binding.cameraButton.setOnClickListener {
//            moveActivity(ArActivity())
//        }
//
//        // 홈 버튼 누를 경우 MainActivity 이동
//        binding.homeButton.setOnClickListener {
//            val location = IntArray(2)
//            binding.cookieCharacter.getLocationOnScreen(location)
//            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
//            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
//
//// 백분율로 변환
//            val percentX = location[0].toFloat() / screenWidth
//            val percentY = location[1].toFloat() / screenHeight
//
//            // 쿠키가 작아지는 애니메이션
//            binding.cookieCharacter.animate()
//                .scaleX(0.3f)
//                .scaleY(0.3f)
//                .setDuration(700)
//                .withEndAction {
//                    // 애니메이션이 끝난 후 메인 화면으로 이동
//                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
//                        putExtra("START_X", percentX)
//                        putExtra("START_Y", percentY)
//                    }
//                    startActivity(intent)
//                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//                    requireActivity().finish()
//                }
//                .start()
//        }
//
//
//    }
//
//    private fun updateUI(){
//
//    }
//
//    private fun clearTokens() {
//        val prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE)
//        prefs.edit().clear().apply()
//        Log.i("ClearTokens", "SharedPreferences 값 삭제 완료")
//    }
//
//    private fun moveActivity(activity: Activity) {
//        val intent = Intent(requireContext(), activity::class.java)
//        startActivity(intent)
//    }
//
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null // 메모리 누수 방지
//    }
//
//}

class LoginMainFragment: Fragment(){
    private var _binding: FragmentLoginMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var keystoreHelper: KeystoreHelper
    private var accessToken: String? = null // 메모리에 저장

    override fun onAttach(context: Context) {
        super.onAttach(context)
        keystoreHelper = KeystoreHelper(requireContext())
    }

    // 로그인 성공 시 호출
    private fun handleLoginSuccess(accessToken: String, refreshToken: String){
        // AccessToken 메모리 저장
        this.accessToken = accessToken

        // RefreshToken 암호화 저장
        if (!keystoreHelper.encryptAndSaveToken(refreshToken)) {
            Toast.makeText(requireContext(), "토큰 저장 실패", Toast.LENGTH_SHORT).show()
        }

        // 메인 화면 이동
        parentFragmentManager.commit {
            replace(R.id.frame_layout, OnBoardingFragment())
            addToBackStack(null)
        }
    }

    // 토큰 갱신 시도
    private fun refreshToken() {
        val refreshToken = keystoreHelper.decryptToken()    // 복호화 시도
        if (refreshToken == null) {
            navigateToLogin()   // 토큰 없으면 로그인 화면으로
            return
        }

        // API로 새 Access Token 요청
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val newToken = RetrofitClient.authService.refreshToken(refreshToken)
                accessToken = newToken.accessToken
            } catch (e: Exception) {
                handleTokenRefreshError()   // 실패 시 처리
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}