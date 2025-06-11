package com.example.edugo_fe.Hansel_and_Gretel

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.edugo_fe.BaseFragment
import com.example.edugo_fe.MainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentHanselResultBinding

class HanselResultFragment : BaseFragment<FragmentHanselResultBinding>(FragmentHanselResultBinding::inflate) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = super.onCreateView(inflater, container, savedInstanceState)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isCorrect = arguments?.getBoolean("isCorrect") ?: false
        delayAndShowDialog(view, isCorrect)
    }

    private fun delayAndShowDialog(rootView: View?, isCorrect: Boolean) {
        if (isCorrect) {
            binding.resultLayout.setBackgroundResource(R.drawable.bg_correct)
        } else {
            binding.resultLayout.setBackgroundResource(R.drawable.bg_incorrect)
        }

        // Delay for 1-2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            activity?.let { activity ->
                // Inflate the custom popup layout
                val popupView = LayoutInflater.from(activity).inflate(R.layout.popup_result, null)

                // Create the PopupWindow
                val popupWindow = android.widget.PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                ).apply {
                    elevation = 10f
                }

                // Apply fade-in animation to the popup
                val fadeInAnimation = android.view.animation.AlphaAnimation(0f, 1f).apply {
                    duration = 500 // Animation duration in milliseconds
                    fillAfter = true
                }

                popupView.startAnimation(fadeInAnimation)

                // Show the PopupWindow
                popupWindow.showAtLocation(rootView, android.view.Gravity.CENTER, 0, 0)

                val firstText = popupView.findViewById<TextView>(R.id.text_first)
                val secondText = popupView.findViewById<TextView>(R.id.text_second)
                val stopBtn = popupView.findViewById<Button>(R.id.btn_hansel_stop)
                val nextBtn = popupView.findViewById<Button>(R.id.btn_hansel_next_step)

                // 정답 오답 배경 처리
                if (isCorrect) {
                    firstText.text = "덕분에 길을 잘 찾아갔어!"
                    secondText.text = "앞으로도 잘 부탁해~"
                    stopBtn.text = "그만하기"
                    nextBtn.text = "다음단계"
                } else {
                    firstText.text = "길을 잃었어.."
                    secondText.text = "다시 시도해볼까?"
                    stopBtn.text = "종료하기"
                    nextBtn.text = "재도전"
                }

                // make visible buttons
                binding.exitButton.visibility = View.VISIBLE
                binding.playButton.visibility = View.VISIBLE
                binding.storyContentButton.visibility = View.VISIBLE

                // 그만두기 버튼 클릭 시
                stopBtn.setOnClickListener {
                    popupWindow.dismiss()
                    requireActivity().apply {
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        finish()
                    }
                }

                // 다음단계 버튼 클릭 시
                nextBtn.setOnClickListener {
                    if (nextBtn.text == "재도전") {
                        (activity as HanselMainActivity).setFragment(HanselMainChoiceFragment())
                        popupWindow.dismiss()
                        } else {
                        popupWindow.dismiss()
                        requireActivity().apply {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }, 1500) // 1500 ms = 1.5 seconds
    }
}