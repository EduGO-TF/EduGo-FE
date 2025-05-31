package com.example.edugo_fe

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugo_fe.databinding.ActivityBaseBinding
import com.example.edugo_fe.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 올바른 레이아웃 설정
        val layoutResource = getLayoutResource()
        if (layoutResource != 0) {
            setContentView(layoutResource)
        } else {
            throw IllegalArgumentException("getLayoutResource() must return a valid layout resource ID")
        }

        // BottomNavigationView 초기화
        bottomNavigationView = findViewById(R.id.bottomAppBar)
        setupBottomNavigationView()
    }

    abstract fun getLayoutResource(): Int

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.action_quest -> {
                    // Handle quest action
                    true
                }
                R.id.action_camera -> {
                    val intent = Intent(this, ArActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.action_book -> {
                    // Handle book action
                    true
                }
                else -> false
            }
        }
    }
}