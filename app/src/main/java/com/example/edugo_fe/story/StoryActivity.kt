package com.example.edugo_fe.story

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.edugo_fe.MainActivity
import com.example.edugo_fe.R
import com.example.edugo_fe.databinding.ActivityStoryBinding

class StoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)



    }

    fun setFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.story_frame_layout, fragment)
        }
    }


    fun moveToHome() {
        val intent = Intent(this@StoryActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}