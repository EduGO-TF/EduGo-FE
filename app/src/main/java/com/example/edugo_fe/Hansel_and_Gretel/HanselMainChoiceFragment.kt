package com.example.edugo_fe.Hansel_and_Gretel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.edugo_fe.MainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentHanselMainChoiceBinding
import com.example.edugo_fe.databinding.FragmentStoryStartBinding

class HanselMainChoiceFragment : Fragment() {
    private var _binding: FragmentHanselMainChoiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHanselMainChoiceBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val choices = listOf(binding.choice1, binding.choice2, binding.choice3)

        for (choice in choices) {
            choice.setOnClickListener {
                // Deselect all choices
                choices.forEach { it.isSelected = false }
                // Select the clicked choice
                choice.isSelected = true
                binding.btnNext.isEnabled = true
            }
        }

        // exit 버튼 누를 경우 홈으로
        binding.btnExit.setOnClickListener {
            requireActivity().apply {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                finish()
            }
        }

        // next 버튼 누를 경우 다음 프래그먼트로
        binding.btnNext.setOnClickListener {
            // 정답을 맞출 경우 true 값 다음 프래그먼트로 전달
            if (binding.choice2.isSelected == true) {
                navigateToNextFragment(true)
            } else {
                navigateToNextFragment(false)
            }

        }
    }

    // 정답을 bundle에 담아서 다음 프래그먼트에 전달
    private fun navigateToNextFragment(isCorrect: Boolean) {
        val nextFrags = HanselCorrectFragment()
        val bundle = Bundle().apply {
            putBoolean("isCorrect", isCorrect)
        }
        nextFrags.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.hansel_frame_layout, nextFrags)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }
}