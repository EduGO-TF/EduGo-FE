package com.example.edugo_fe.Login

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.edugo_fe.ArActivity
import com.example.edugo_fe.MainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentLoginMainBinding
import com.example.edugo_fe.databinding.FragmentOnBoardingBinding

class OnBoardingFragment : Fragment() {
    private var _binding : FragmentOnBoardingBinding?= null
    private val binding get() = _binding!!  // 안전하게 접근할 수 있도록 설정

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraButton.setOnClickListener {
            moveActivity(ArActivity())
        }

        binding.homeButton.setOnClickListener {
            moveAnimation()
        }
    }

    // Activity 이동
    private fun moveActivity(activity: Activity) {
        val intent = Intent(requireContext(), activity::class.java)
        startActivity(intent)
    }

    // 애니메이션 적용
    private fun moveAnimation() {
        val location = IntArray(2)
        binding.cookieCharacter.getLocationOnScreen(location)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        // 백분율로 변환
        val percentX = location[0].toFloat() / screenWidth
        val percentY = location[1].toFloat() / screenHeight

        // 쿠키가 작아지는 애니메이션
        binding.cookieCharacter.animate()
            .scaleX(0.4f)
            .scaleY(0.4f)
            .setDuration(700)
            .withEndAction {
                // 애니메이션이 끝난 후 메인 화면으로 이동
                val intent = Intent(requireContext(), MainActivity::class.java)
//                    putExtra("START_X", percentX)
//                    putExtra("START_Y", percentY)
//                    Log.d("AIResponse", "OnBoardingFragment : ${percentX}")
//                    Log.d("AIResponse", "OnBoardingFragment : ${percentY}")

                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                requireActivity().finish()
            }
            .start()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }

}