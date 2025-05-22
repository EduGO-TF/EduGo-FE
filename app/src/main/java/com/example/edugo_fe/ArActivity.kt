package com.example.edugo_fe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edugo_fe.databinding.ActivityArBinding
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

class ArActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArBinding
    private lateinit var arSession: Session
    private var modelNode: ModelNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        checkARCoreSupport()

        // ARSession 초기화
        arSession = try {
            Session(this).apply {
                configure(Config(this).apply {
                    depthMode = Config.DepthMode.AUTOMATIC // Depth API 활성화
                })
            }
        } catch (e: Exception) {
            Log.e("ARActivity", "ARSession initialization failed: ${e.message}")
            null
        } ?: return

        // ✅ SceneView will handle session internally
        binding.arSceneView.configureSession { session, config ->
            config.depthMode = Config.DepthMode.AUTOMATIC
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            session.configure(config)
        }

        // ARSceneView와 연결 (생명주기 관리 자동 설정)
//        binding.arSceneView.lifecycle.addObserver(binding.arSceneView)


        if (arSession != null) {
            binding.arSceneView.configureSession { session, config ->
                config.depthMode = com.google.ar.core.Config.DepthMode.AUTOMATIC // Depth API 활성화
                session.configure(config)
            }
        } else {
            Toast.makeText(this, "Failed to initialize ARCore session", Toast.LENGTH_SHORT).show()
        }

        Log.d("ARActivityarar", "ARSession is initialized: ${::arSession.isInitialized}")

        binding.composeView.setContent {
            My3DScene(binding.arSceneView)
        }
        val session = com.google.ar.core.Session(this)
        val isDepthSupported = session.isDepthModeSupported(com.google.ar.core.Config.DepthMode.AUTOMATIC)
        Log.d("ARActivityarar", "Depth API supported: $isDepthSupported")
        if (!isDepthSupported) {
            Toast.makeText(this, "Depth API is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
        Log.d("ARActivityarar", "ARSession is null: ${binding.arSceneView.session == null}")
        Log.d("ARActivityarar", "ModelNode is null: ${modelNode == null}")

    }

    override fun onResume() {
        super.onResume()
        try {
            if (::binding.isInitialized) {
                arSession.resume()
                binding.arSceneView.session?.resume() // ARSession 수동 Resume
                Log.d("ARActivityarar", "ARSceneView resumed successfully.")
            }
        } catch (e: Exception) {
            Log.e("ARActivityarar", "Failed to resume ARSession: ${e.message}")
        }
    }

    override fun onPause() {
        try {
            // ARSession Pause
            if (::arSession.isInitialized) {
                arSession.pause()
                Log.d("ARActivityarar", "ARSceneView paused successfully.")
            }
        } catch (e: Exception) {
            Log.e("ARActivityarar", "Failed to pause ARSession: ${e.message}")
        }
        super.onPause()
    }

    override fun onDestroy() {
        try {
            // ARSceneView Destroy
            binding.arSceneView.destroy()
        } catch (e: Exception) {
            Log.e("ARActivityarar", "Failed to destroy ARSceneView: ${e.message}")
        }
        super.onDestroy()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }
    }

    private fun checkARCoreSupport() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (!availability.isSupported) {
            Toast.makeText(this, "ARCore is not supported on this device.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    @Composable
    fun My3DScene(arSceneView: ARSceneView) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)

        // 모델 로드
        binding.arSceneView.onSessionUpdated = { session, frame ->
            // 세션 업데이트 시 실행할 작업
            if (modelNode == null) {
                try {
                    val modelInstance = modelLoader.createModelInstance(R.raw.sofa)
                    modelNode = ModelNode(modelInstance = modelInstance).apply {
                        position = Position(0.0f, 0.0f, -2.0f)
                    }
                    binding.arSceneView.addChildNode(modelNode!!)
                    Log.d("ARActivityarar", "ModelNode successfully added during session update.")
                } catch (e: Exception) {
                    Log.e("ARActivityarar", "Failed to load model or add ModelNode: ${e.message}")
                }
            }
        }

        modelNode?.let { node ->
            try {
                arSceneView.removeChildNode(node)
                arSceneView.addChildNode(node)
                Log.d("ARActivityarar", "Model successfully added to ARSceneView.")
            } catch (e: Exception) {
                Log.e("ARActivityarar", "Failed to add model: ${e.message}")
            }
        }
    }
}