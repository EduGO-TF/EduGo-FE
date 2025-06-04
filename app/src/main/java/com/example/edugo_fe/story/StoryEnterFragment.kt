package com.example.edugo_fe.story

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.edugo_fe.Login.LoginActivity
import com.example.edugo_fe.databinding.FragmentStoryEnterBinding

class StoryEnterFragment : Fragment() {
    private var _binding: FragmentStoryEnterBinding? = null
    private val binding get() = _binding!!

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

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // 메모리 누수 방지
    }

}