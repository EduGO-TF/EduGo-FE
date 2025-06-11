package com.example.edugo_fe.story

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.edugo_fe.Hansel_and_Gretel.HanselMainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentStoryEnterBinding

class StoryEnterFragment : Fragment() {
    private var _binding: FragmentStoryEnterBinding? = null
    private val binding get() = _binding!!
    private val storyImageList = listOf(R.drawable.hansel_and_gretel_story_image_1, R.drawable.hansel_and_gretel_story_image_3, R.drawable.hansel_and_gretel_story_image_2)
    private val countImageList = listOf(R.drawable.count_1_3, R.drawable.count_2_3, R.drawable.count_3_3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStoryEnterBinding.inflate(inflater, container, false)

        binding.storyTitle.bringToFront()
        binding.storyEnterButton.bringToFront()

        binding.exitButton.setOnClickListener {
            (activity as StoryActivity).moveToHome()
        }

        binding.storyEnterButton.setOnClickListener {
            Log.d("AIResponse", "Clicked!!")
            showPopupStory()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            handleBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }

    private fun showPopupStory() {
        var count = 0
        val popupView = layoutInflater.inflate(R.layout.popup_hansel_and_gretel_story_dialog, null)
        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(binding.root, android.view.Gravity.CENTER, 0, 0)

        val btnNext = popupView.findViewById<ImageButton>(R.id.btn_story_next)
        val btnBefore = popupView.findViewById<ImageButton>(R.id.btn_story_before)
        val storyImage = popupView.findViewById<ImageView>(R.id.story_image)
        val countImage = popupView.findViewById<ImageView>(R.id.count_image)

        // Before Button
        btnBefore.setOnClickListener {
            count -= 1
            when(count){
                0 -> {
                    btnNext.visibility = View.VISIBLE
                    btnBefore.visibility = View.INVISIBLE
                    storyImage.setImageResource(storyImageList[count])
                    countImage.setImageResource(countImageList[count])
                }
                1 -> {
                    btnNext.visibility = View.VISIBLE
                    btnBefore.visibility = View.VISIBLE
                    storyImage.setImageResource(storyImageList[count])
                    countImage.setImageResource(countImageList[count])
                }
                else -> {
                    count = 0
                }
            }
        }

        // Next Button
        btnNext.setOnClickListener {
            count += 1
            when(count){
                1 -> {
                    btnNext.visibility = View.VISIBLE
                    btnBefore.visibility = View.VISIBLE
                    storyImage.setImageResource(storyImageList[count])
                    countImage.setImageResource(countImageList[count])
                }
                2 -> {
                    btnNext.visibility = View.INVISIBLE
                    btnBefore.visibility = View.VISIBLE
                    storyImage.setImageResource(storyImageList[count])
                    countImage.setImageResource(countImageList[count])
                }
                else -> {
                    count = 2
                }
            }
        }


        // 입장 버튼 누를 경우
        popupView.findViewById<ImageButton>(R.id.btn_main_question_enter).setOnClickListener {
            requireActivity().apply {
                startActivity(Intent(requireContext(), HanselMainActivity::class.java))
                finish()
            }
        }

        binding.storyModal.visibility = View.INVISIBLE

        // 뒤로가기 해서 팝업이 사라질 경우
        popupWindow.setOnDismissListener {
            binding.storyModal.apply {
                alpha = 0f // 투명도를 0으로 설정
                visibility = View.VISIBLE // 보이도록 설정
                animate()
                    .alpha(1f) // 투명도를 점차 1로 증가
                    .setDuration(300) // 애니메이션 지속 시간 (밀리초)
                    .start()
            }
        }
    }

    private fun handleBackPressed() {
        (activity as? StoryActivity)?.moveToHome()
    }

}