package com.example.edugo_fe

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.ApiData.Detection
import com.example.edugo_fe.ApiData.DetectionsResponse
import com.example.edugo_fe.databinding.ActivityArBinding
import com.example.edugo_fe.network.ApiClient
import com.google.android.filament.View
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.rotation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.rotation
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.collision.Quaternion
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
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
    private lateinit var back_button : FloatingActionButton

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

        instructionText = binding.instructionText
        back_button = binding.backButton

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

        captureArImageAndSend() // 3초 후 캡쳐 시작

        back_button.setOnClickListener {
            onBackPressed()
        }

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

//                    // 2. 위치 계산 (카메라 위치 + 정방향 1m)
//                    val position = floatArrayOf(
//                        pose.tx() + forward[0],
//                        pose.ty() + forward[1],
//                        pose.tz() + forward[2]
//                    )
//
//                    // 3. 회전 쿼터니언 추출
//                    val rotationQuaternion = FloatArray(4).apply {
//                        pose.getRotationQuaternion(this, 0)
//                    }

                    val rotation = FloatArray(4).apply {
                        pose.getRotationQuaternion(this, 0)
                        // 수동 쿼터니언 조정 (Y축 90도)
                        val angle = Math.toRadians(90.0).toFloat()
                        this[0] = 0f // x
                        this[1] = sin(angle / 2) // y
                        this[2] = 0f // z
                        this[3] = cos(angle / 2) // w
                    }

                    val anchor = arSession!!.createAnchor(
                        com.google.ar.core.Pose(
                            floatArrayOf(
                                pose.tx() + forward[0],
                                pose.ty() + forward[1],
                                pose.tz() + forward[2]
                            ),
                            rotation
                        )
                    )
//                    addAnchorNode(a

//                    val anchor = arSession!!.createAnchor(
//                        com.google.ar.core.Pose(
//                            position,
//                            rotationQuaternion // 수정된 회전 데이터
//                        )
//                    )
                    addAnchorNode(anchor)
                    Log.d("AIResponse", "앵커 생성 성공")
                } catch (e: Exception) {
                    Log.e("AIResponse", "앵커 생성 실패: ${e.message}")
                }
            }
        }
    }


    override fun onBackPressed() {
        // AR 세션이 활성화된 경우 우선 정리
        if (::sceneView.isInitialized && arSession != null) {
            cleanupARSession()
        }
        super.onBackPressed()
    }

    private fun cleanupARSession() {
        sceneView.destroy()
        arSession?.close()
        arSession = null
        anchorNode?.anchor?.detach()
        anchorNode = null
    }


    // StoryActivity로 이동
    private fun moveToStory(){
        val intent = Intent(this@ArActivity, StoryActivity::class.java)
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
                            modelNode.onTouch = {motionEvent, hitResult ->
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

            val surfaceView = binding.arSceneView // 적절한 SurfaceView 참조
            Log.d("AIResponse", "surfaceView width: ${surfaceView.width}, height: ${surfaceView.height}")
            // ARSceneView 캡쳐
            val bitmap = Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.ARGB_8888)
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

    // Bitmap을 파일로 변환
    private fun sendImageToServer(bitmap: Bitmap) {
        val file = File(cacheDir, "ar_image.jpg")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }

        // 파일을 requestBody로 변환
        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        // Retrofit 호출
        val apiService = ApiClient.createApiService()
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
        detections.forEach { detection ->
            Log.d("AIResponse", "Class ID: ${detection.class_id}")
            Log.d("AIResponse", "Confidence: ${detection.confidence}")
        }

        // 특정 조건에서 캐릭터 생성
        val highestConfidenceDetection = detections.maxByOrNull { it.confidence }
        highestConfidenceDetection?.let { detection ->
            if (detection.class_id == 0 && detection.confidence > 0.4f) {
                showCharacterDirectly()
                Toast.makeText(this, "배경 [숲] 인식 완료!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "배경 인식 안됨 x", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 생명 주기 관리
    override fun onResume() {
        super.onResume()
//        try {
//            arSession?.resume()
//            sceneView.onSessionResumed
//        } catch (e: Exception) {
//            Log.e("ArActivity", "Error resuming ARSession: ${e.message}")
//           }
    }

    override fun onPause() {
        super.onPause()
//        try {
//            arSession?.pause()
//            sceneView.onSessionPaused
//            Log.d("AIResponse", "Paused")
//        } catch (e: Exception) {
//            Log.e("ArActivity", "Error pausing ARSession: ${e.message}")
//        }
    }

    override fun onStop() {
        // 액티비티가 완전히 종료될 때 리소스 정리
        if (isFinishing) {
            sceneView.destroy()
            arSession?.close()
        }
        super.onStop()
    }

    override fun onDestroy() {
        // 백업 정리 로직
        if (!isFinishing) {
            cleanupARSession()
        }
        super.onDestroy()
        Log.d("AIResponse", "최종 종료 완료")
    }
}