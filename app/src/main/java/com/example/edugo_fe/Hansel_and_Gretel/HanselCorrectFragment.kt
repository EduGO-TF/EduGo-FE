package com.example.edugo_fe.Hansel_and_Gretel

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.edugo_fe.MainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentHanselCorrectBinding
import com.example.edugo_fe.databinding.FragmentHanselMainChoiceBinding

class HanselCorrectFragment : Fragment() {
    private var _binding: FragmentHanselCorrectBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHanselCorrectBinding.inflate(inflater, container, false)

        checkAnswer()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // exit 버튼 누를 경우 홈으로
        binding.btnExit.setOnClickListener {
            requireActivity().apply {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                finish()
            }
        }
    }

    // 정답인지 아닌 지 확인하는 함수
    private fun checkAnswer() {
        val isCorrect = arguments?.getBoolean("isCorrect") ?: false
        if (isCorrect){
            binding.isCorrectLayout.isSelected = true
        } else {
            binding.isCorrectLayout.setBackgroundResource(R.drawable.incorrect_layout)
            binding.isCorrectComment.apply {
                setText(R.string.incorrect)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.incorrect))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }
}