package com.example.edugo_fe

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugo_fe.Login.KeystoreHelper
import com.example.edugo_fe.Login.LoginActivity
import com.example.edugo_fe.Login.SecurePrefs
import com.example.edugo_fe.databinding.ActivityMainBinding
import com.kakao.sdk.user.UserApiClient
import kotlin.random.Random

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val random = Random.Default
    private var isAnimating = true
    private var moveX: ObjectAnimator? = null
    private var moveY: ObjectAnimator? = null
    private var dX = 0f
    private var dY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setBaseContent(R.layout.activity_main)
        setContentView(binding.root)

        setCharacterStartPosition()

        enableEdgeToEdge()

        // 바텀 네비게이션 첫 화면 설정
        binding.bottomNavigation.selectedItemId = R.id.nav_dummy

        // AccessToken이 존재하지 않을 경우 LoginActivity로 이동
        // 해당 부분 로그인 가능해지면 수정요망 == 으로
        if (SecurePrefs.getAccessToken(this) != null) {
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

            // Character Interaction
            setupCharacterInteraction()

            // Set up background
            mainLayout.setBackgroundResource(R.drawable.bg_sky_and_ground) // Replace with your drawable
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

    // Set character click control
    @SuppressLint("ClickableViewAccessibility")
    private fun setupCharacterInteraction() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener(){
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                showPopupAboveCharacter()
//                isAnimating = false
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                // 드래그 시작 (기본 동작)
            }
        })

        binding.character.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isAnimating = false // 랜덤 움직임 중지
                    moveX?.cancel() // 애니메이션 취소
                    moveY?.cancel() // 애니메이션 취소
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    isAnimating = true // 랜덤 움직임 재개
                    startRandomMovement(binding.character, binding.mainLayout.width, binding.mainLayout.height)
                }
            }
            true
        }
    }

    private fun showPopupAboveCharacter() {
        val popupView = layoutInflater.inflate(R.layout.popup_closet_dialog, null)
        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // 캐릭터 바로 위에 위치시키기
        val location = IntArray(2)
        binding.character.getLocationOnScreen(location)
        val characterX = location[0]
        val characterY = location[1]

        // Popup 위치 조정
        val offsetX = - binding.character.width / 3 - popupView.measuredWidth / 2
        val offsetY = - binding.character.height / 5 - popupView.measuredHeight
        popupWindow.showAtLocation(binding.character, Gravity.NO_GRAVITY, characterX + offsetX, characterY + offsetY)

        popupView.findViewById<ImageButton>(R.id.change_clothes_button).setOnClickListener {
            startActivity(Intent(this@MainActivity, ClosetActivity::class.java))
            popupWindow.dismiss()
        }
        isAnimating = false
        moveX?.cancel()
        moveY?.cancel()
    }

    private fun moveArActivity() {
        startActivity(Intent(this, ArActivity::class.java))
    }

    private fun setCharacterStartPosition() {
        // 캐릭터 좌푯값 받고, 아래로 이동
//        val percentX = intent.getFloatExtra("START_X", 0.5f)
//        val percentY = intent.getFloatExtra("START_Y", 0.5f)
//
//        Log.d("AIResponse", "MainActivity : ${percentX}")
//        Log.d("AIResponse", "MainActivity : ${percentY}")
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // 상대 좌표 변환 (절대값 ->  %)
//        val startX = percentX * screenWidth - binding.character.width / 2 // 가운데 정렬
//        val startY = percentY * screenHeight - binding.character.height / 2
        val startX = (screenWidth / 3 - binding.character.width / 2).toFloat()
        val startY = (screenHeight / 3 - binding.character.height).toFloat()
        binding.character.translationX = startX
        binding.character.translationY = startY

        binding.character.animate()
            .translationY(startY + 100f)
            .setDuration(1000)
            .start()
    }

    private fun startRandomMovement(character: View, screenWidth: Int, screenHeight: Int) {
        if (!isAnimating) return

        // Define the ground area (bottom third of the screen)
        val groundTop = screenHeight * 2 / 3
        val groundBottom = screenHeight - character.height
        val groundLeft = 0
        val groundRight = screenWidth - character.width

        fun moveToRandomPosition() {
            if (!isAnimating) {
                character.clearAnimation()
                return
            }
            moveX?.cancel()
            moveY?.cancel()

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

            moveX.apply {
                addListener(onEnd = {
                     moveToRandomPosition() })  // 상태 확인 후 재시작
                start()
            }
            moveY.start()

        }

        fun stopAnimation() {
            moveX?.apply {
                cancel()
                removeAllListeners()
            }
            moveY?.apply {
                cancel()
                removeAllListeners()
            }

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