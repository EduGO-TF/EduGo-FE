package com.example.edugo_fe

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugo_fe.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val random = Random.Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mainLayout = binding.mainLayout

//         AR Button 누를 시 activity 이동
        binding.mArButton.setOnClickListener {
            moveArActivity()
        }

        // Set up background
        mainLayout.setBackgroundResource(R.drawable.bg_sky_and_ground) // Replace with your drawable

        val character = ImageView(this).apply {
            setImageResource(R.drawable.gingerbread) // Replace with your character drawable
            layoutParams = ConstraintLayout.LayoutParams(300, 300) // Character size
        }
        mainLayout.addView(character)

        // Ensure layout is ready before animating
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (mainLayout.width > 0 && mainLayout.height > 0) {
                    // Set character's initial random position within the ground area
                    setCharacterStartPosition(character, mainLayout.width, mainLayout.height)

                    startRandomMovement(character, mainLayout.width, mainLayout.height)
                    mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun moveArActivity() {
        startActivity(Intent(this, ArActivity::class.java))
    }

    private fun setCharacterStartPosition(character: View, screenWidth: Int, screenHeight: Int) {
        // Define the ground area (bottom third of the screen)
        val groundTop = screenHeight * 2 / 3
        val groundBottom = screenHeight - character.height
        val groundLeft = 0
        val groundRight = screenWidth - character.width

        // Set random initial position
        val startX = random.nextInt(groundLeft, groundRight).toFloat()
        val startY = random.nextInt(groundTop, groundBottom).toFloat()
        character.translationX = startX
        character.translationY = startY
    }

    private fun startRandomMovement(character: View, screenWidth: Int, screenHeight: Int) {
        // Define the ground area (bottom third of the screen)
        val groundTop = screenHeight * 2 / 3
        val groundBottom = screenHeight - character.height
        val groundLeft = 0
        val groundRight = screenWidth - character.width

        fun moveToRandomPosition() {
            // Generate random target position within the ground area
            val targetX = random.nextInt(groundLeft, groundRight).toFloat()
            val targetY = random.nextInt(groundTop, groundBottom).toFloat()

            // Random duration for movement
            val duration = random.nextLong(4000, 6000) // Between 2 and 4 seconds

            // Animate X and Y movement
            val moveX = ObjectAnimator.ofFloat(character, "translationX", character.translationX, targetX)
            val moveY = ObjectAnimator.ofFloat(character, "translationY", character.translationY, targetY)

            moveX.duration = duration
            moveY.duration = duration

            // Start both animations
            moveX.start()
            moveY.start()

            // Schedule the next random movement after the current one finishes
            moveX.addListener(onEnd = { moveToRandomPosition() })
        }

        // Start the initial random movement
        moveToRandomPosition()
    }
}

// Extension function to simplify adding animation listeners
private fun ObjectAnimator.addListener(
    onEnd: (() -> Unit)? = null
) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) {}
        override fun onAnimationEnd(animation: android.animation.Animator) {
            onEnd?.invoke()
        }

        override fun onAnimationCancel(animation: android.animation.Animator) {}
        override fun onAnimationRepeat(animation: android.animation.Animator) {}
    })
}