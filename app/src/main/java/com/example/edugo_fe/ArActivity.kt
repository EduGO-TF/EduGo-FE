package com.example.edugo_fe

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.ApiData.Detection
import com.example.edugo_fe.ApiData.DetectionsResponse
import com.example.edugo_fe.databinding.ActivityArBinding
import com.example.edugo_fe.network.ApiClient
import com.example.edugo_fe.network.ApiService
import com.example.edugo_fe.story.StoryActivity
import com.google.android.filament.View
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

class ArActivity : AppCompatActivity() {

    private lateinit var sceneView: ARSceneView
    private lateinit var instructionText: TextView
    private var arSession: Session? = null
    private lateinit var arIng: ImageView

    private lateinit var binding: ActivityArBinding

    private var anchorNode: AnchorNode? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    var anchorNodeView: View? = null

    private var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    private fun updateInstructions() {
        instructionText.text = trackingFailureReason?.let {
            it.getDescription(this)
        } ?: if (anchorNode == null) {
            getString(R.string.start_ment)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        arIng = binding.arIng
        instructionText = binding.instructionText

        // ARSceneView 초기화를 onCreate에서 즉시 수행
        sceneView = binding.arSceneView.apply {
            lifecycle = this@ArActivity.lifecycle
            planeRenderer.isEnabled = false
            configureSession { session, config ->
                arSession = session  // 세션 변수 직접 할당
                config.depthMode = Config.DepthMode.DISABLED
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                // 광안 설정
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }

            onTrackingFailureChanged = { reason ->
                this@ArActivity.trackingFailureReason = reason
            }
        }

        binding.storyButton.setOnClickListener {
            startActivity(Intent(this@ArActivity, StoryActivity::class.java))
            finish()
        }

//        captureArImageAndSend() // 3초 후 캡쳐 시작
    }

    private fun showCharacterDirectly() {
        if (anchorNode == null) {
            createFixedPositionAnchor()
        } else {
            Log.e("AIResponse", "AnchorNode 있음")
        }
    }

    private fun createFixedPositionAnchor() {
        sceneView.frame?.let { frame ->
            if (frame.camera.trackingState == TrackingState.TRACKING) {
                try {
                    val pose = frame.camera.pose

                    // 1. 정방향 벡터 계산 (카메라가 바라보는 방향)
                    val forward = FloatArray(3).apply {
                        // (0,0,-1) 벡터를 Pose의 회전 행렬로 변환
                        pose.transformPoint(
                            floatArrayOf(0f, 0f, -1f), 0, // 원본 벡터
                            this, 0                       // 결과 저장 배열
                        )
                    }

                    val rotation = FloatArray(4).apply {
                        pose.getRotationQuaternion(this, 0)
                        // 수동 쿼터니언 조정 (Y축 90도)
                        val angle = Math.toRadians(90.0).toFloat()
                        this[0] = 0f // x
                        this[1] = sin(angle / 2) // y
                        this[2] = 0f // z
                        this[3] = cos(angle / 2) // w
                    }

                    arSession?.let { session ->
                        val anchor = session.createAnchor(
                            com.google.ar.core.Pose(
                                floatArrayOf(
                                    pose.tx() + forward[0],
                                    pose.ty() + forward[1],
                                    pose.tz() + forward[2]
                                ),
                                rotation
                            )
                        )
                        addAnchorNode(anchor)
                        Log.d("AIResponse", "앵커 생성 성공")
                    }

                } catch (e: Exception) {
                    Log.e("AIResponse", "앵커 생성 실패: ${e.message}")
                }
            }
        }
    }

    // StoryActivity로 이동
    private fun moveToStory() {
        val intent = Intent(this@ArActivity, StoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        intent.putExtra("MODEL_NAME", "gingerbread") // 필요한 데이터를 전달
        startActivity(intent)
        finish()
    }

    // AnchorNode 추가하기
    private fun addAnchorNode(anchor: Anchor) {
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    lifecycleScope.launch {
                        buildModelNode()?.let { modelNode ->
                            // 1. ModelNode 클릭 리스너 추가
                            modelNode.onTouch = { motionEvent, hitResult ->
                                moveToStory()
                                true
                            }
                            addChildNode(modelNode)
                        }
                    }
                    anchorNode = this
                }
        )
    }

    // ModelNode 객체 만들기
    private suspend fun buildModelNode(): ModelNode? {
        sceneView.modelLoader.loadModelInstance(
            "https://edugo-tf.github.io/EduGo-FE/assets/models/gingerbread_man.glb"
        )?.let { modelInstance ->
            return ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.7f,
                centerOrigin = Position(y = -0.5f)
            ).apply {
                // Y축 기준 90도 회전 (Euler angles 방식)
                rotation = Position(y = 90f) // ★ 핵심 수정 부분
                isEditable = true
            }
        }
        return null
    }

    // 이미지 캡쳐
    private fun captureArImageAndSend() {
        lifecycleScope.launch {
            // 3초 대기
            kotlinx.coroutines.delay(3000)
            Log.d("AIResponse", "대기는 함")

            if (isActive){
                val surfaceView = sceneView // 적절한 SurfaceView 참조
                // ARSceneView 캡쳐
                val bitmap =
                    Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.ARGB_8888)
                PixelCopy.request(surfaceView, bitmap, { result ->
                    if (result == PixelCopy.SUCCESS) {
                        Log.d("AIResponse", "Image Capture!")
                        sendImageToServer(bitmap) // 서버로 이미지 전송
                    } else {
                        Log.e("AIResponse", "Failed to capture AR image.")
                    }
                }, android.os.Handler(Looper.getMainLooper()))
            }
        }
    }

    // Bitmap을 파일로 변환
    private fun sendImageToServer(bitmap: Bitmap) {
        val file = File(cacheDir, "ar_image.jpg")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }

        // 파일을 requestBody로 변환
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull()!!)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        // Retrofit 호출
        val apiService = ApiClient.createService(ApiService::class.java)
        val call = apiService.getForest(body)
        call.enqueue(object : Callback<DetectionsResponse> {
            override fun onResponse(
                call: Call<DetectionsResponse>,
                response: Response<DetectionsResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { detectionsResponse ->
                        handleDetections(detectionsResponse.detections)
                    }
                } else {
                    Log.e("AIResponse", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DetectionsResponse>, t: Throwable) {
                Log.e("AIResponse", "Failed to send image: ${t.message}")
            }
        })
    }

    // Detecion List 데이터 다루기
    private fun handleDetections(detections: List<Detection>) {
        // 로그 출력
        if (detections.isEmpty()) {
            Log.d("AIResponse", "탐지된 객체가 없습니다.")
            arIng.visibility = android.view.View.INVISIBLE
            instructionText.text = "탐지된 객체가 없습니다"
            Toast.makeText(this, "탐지 실패", Toast.LENGTH_SHORT).show()
            return
        }

        detections.forEach { detection ->
            Log.d("AIResponse", "Class ID: ${detection.class_id}")
            Log.d("AIResponse", "Confidence: ${detection.confidence}")
        }

        // 탐지된 객체 중에서 가장 높은 confidence 값을 가진 객체 확인
        val highestConfidenceDetection = detections.maxByOrNull { it.confidence }
        if (highestConfidenceDetection == null || highestConfidenceDetection.confidence <= 0.4f || highestConfidenceDetection.class_id != 0) {
            Log.d("AIResponse", "탐지된 객체가 조건에 부합하지 않습니다.")
            arIng.visibility = android.view.View.INVISIBLE
            instructionText.text = "탐지된 객체가 조건에 부합하지 않습니다"
            Toast.makeText(this, "조건에 맞는 객체 탐지 실패", Toast.LENGTH_SHORT).show()
            return
        }

        // 조건 만족 시 캐릭터 생성
        showCharacterDirectly()
        arIng.visibility = android.view.View.INVISIBLE
        instructionText.visibility = android.view.View.INVISIBLE
        Toast.makeText(this, "배경 [숲] 인식 완료!", Toast.LENGTH_SHORT).show()
    }

    private fun cleanupARSession() {
        // Coroutine 취소
        lifecycleScope.cancel()

        // AR 리소스 정리
        arSession?.close()
        arSession = null
        anchorNode?.anchor?.detach()
        anchorNode = null
//        sceneView.destroy()
    }


    // 생명 주기 관리 -> 자동으로 생명주기를 관리하기 때문에 다른 걸 넣을 필요가 없음
//    override fun onResume() {
//        Log.d("AIResponse", "Resume!")
//        super.onResume()
//        arSession?.let {
//            sceneView.onSessionResumed(it)
//        }
//    }
//
//    override fun onPause() {
//        Log.d("AIResponse", "Pause!")
//        super.onPause()
//        arSession?.let {
//            sceneView.onSessionPaused(it)
//        }
//    }

//    override fun onDestroy() {
//        Log.d("AIResponse", "Destroy!")
//        // 액티비티가 완전히 종료될 때 리소스 정리
//        cleanupARSession()
//        super.onDestroy()
//    }
}
