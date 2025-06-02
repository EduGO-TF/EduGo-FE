package com.example.edugo_fe

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugo_fe.Login.KeystoreHelper
import com.example.edugo_fe.Login.LoginActivity
import com.example.edugo_fe.Login.SecurePrefs
import com.example.edugo_fe.databinding.ActivityMainBinding
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlin.random.Random

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val random = Random.Default

    override fun getLayoutResource(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // AccessToken이 존재하지 않을 경우 LoginActivity로 이동
        if (SecurePrefs.getAccessToken(this) == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            val mainLayout = binding.mainLayout

            //  AR Button 누를 시 activity 이동
            binding.mArButton.setOnClickListener {
                moveArActivity()
            }

            // Back Button 누를 경우 로그아웃
            binding.logoutButton.setOnClickListener {
                // 앱 내부 토큰 삭제
                logout(this)

                // Kakao 서버 로그아웃
                UserApiClient.instance.logout { error ->
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finishAffinity()
                }
            }

            // Set up background
            mainLayout.setBackgroundResource(R.drawable.bg_sky_and_ground) // Replace with your drawable

//        val character = ImageView(this).apply {
//            setImageResource(R.drawable.ginger_character) // Replace with your character drawable
//            layoutParams = ConstraintLayout.LayoutParams(300, 300) // Character size
//        }
//        mainLayout.addView(character)


            // Ensure layout is ready before animating
            mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (mainLayout.width > 0 && mainLayout.height > 0) {
                        // Set character's initial random position within the ground area
//                    setCharacterStartPosition(character, mainLayout.width, mainLayout.height)
                        val character = binding.character

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


    }



    private fun moveArActivity() {
        startActivity(Intent(this, ArActivity::class.java))
    }

    private fun setCharacterStartPosition() {
        // Define the ground area (bottom third of the screen)
//        val groundTop = screenHeight * 2 / 3
//        val groundBottom = screenHeight - character.height
//        val groundLeft = 0
//        val groundRight = screenWidth - character.width
//
//        // Set random initial position
//        val startX = random.nextInt(groundLeft, groundRight).toFloat()
//        val startY = random.nextInt(groundTop, groundBottom).toFloat()
//        character.translationX = startX
//        character.translationY = startY

        // 캐릭터 좌푯값 받고, 아래로 이동
        val percentX = intent.getFloatExtra("START_X", 0.5f)
        val percentY = intent.getFloatExtra("START_Y", 0.5f)

        Log.d("Coord", "$percentX")
        Log.d("Coord", "$percentY")
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // 상대 좌표 변환 (절대값 ->  %)
        val startX = percentX * screenWidth - binding.character.width / 2 // 가운데 정렬
        val startY = percentY * screenHeight - binding.character.height / 2
        binding.character.translationX = startX
        binding.character.translationY = startY

        binding.character.animate()
            .translationY(startY + 100f)
            .setDuration(1000)
            .start()
    }

    private fun startRandomMovement(character: View, screenWidth: Int, screenHeight: Int) {
        setCharacterStartPosition()

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

    // 로그아웃 구현
    private fun logout(context: Context) {
        SecurePrefs.clearAccessToken(context)
        KeystoreHelper(context).deleteToken()
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