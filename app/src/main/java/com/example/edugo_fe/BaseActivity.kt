package com.example.edugo_fe

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 올바른 레이아웃 설정
        setContentView(R.layout.activity_base)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        // BottomNavigationView 초기화
        setupBottomNavigationView()
    }

    fun setBaseContent(layoutRes: Int) {
        val baseContent = findViewById<FrameLayout>(R.id.base_content)
        baseContent.removeAllViews() // 기존 View 제거
        val content = layoutInflater.inflate(layoutRes, baseContent, false)
        baseContent.addView(content)
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.action_quest -> {
                    // 퀘스트 화면 이동
                    true
                }
                R.id.action_book -> {
                    // 도감 화면 이동
                    true
                }
                else -> false
            }
        }
    }
}