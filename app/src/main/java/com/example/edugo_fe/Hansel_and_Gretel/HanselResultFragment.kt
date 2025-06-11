package com.example.edugo_fe.Hansel_and_Gretel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.edugo_fe.BaseFragment
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.FragmentHanselResultBinding

class HanselResultFragment : BaseFragment<FragmentHanselResultBinding>(FragmentHanselResultBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hansel_result, container, false)
    }
}