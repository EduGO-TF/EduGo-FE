package com.example.edugo_fe.Hansel_and_Gretel

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.ActivityHanselMainBinding

class HanselMainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHanselMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHanselMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        setFragment(HanselMainStartFragment())
    }

    fun setFragment(fragment: Fragment){
        supportFragmentManager.commit {
            replace(R.id.hansel_frame_layout, fragment)
        }
    }
}