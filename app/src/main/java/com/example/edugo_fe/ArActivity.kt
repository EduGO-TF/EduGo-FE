package com.example.edugo_fe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.edugo_fe.databinding.ActivityArBinding
import com.google.android.filament.View
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.launch

class ArActivity : AppCompatActivity() {

    private lateinit var sceneView: ARSceneView
    private lateinit var instructionText: TextView
    private var arSession: Session? = null

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
        sceneView = binding.arSceneView.apply {
            lifecycle = this@ArActivity.lifecycle
            planeRenderer.isEnabled = true
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
            onSessionUpdated = { _, frame ->
                if (anchorNode == null) {
                    frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { plane ->
                            addAnchorNode(plane.createAnchor(plane.centerPose))
                        }
                }
            }

            onTrackingFailureChanged = { reason ->
                this@ArActivity.trackingFailureReason = reason
            }
        }


    }

    private fun moveToStory(){
        val intent = Intent(this@ArActivity, StoryActivity::class.java)
        intent.putExtra("MODEL_NAME", "gingerbread") // 필요한 데이터를 전달
        startActivity(intent)
    }

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

    private suspend fun buildModelNode(): ModelNode? {
        sceneView.modelLoader.loadModelInstance(
            "https://edugo-tf.github.io/EduGo-FE/assets/models/gingerbread_man.glb"
        )?.let { modelInstance ->
            return ModelNode(
                modelInstance = modelInstance,
                // Scale to fit in a 0.5 meters cube
                scaleToUnits = 0.7f,
                // Bottom origin instead of center so the model base is on floor
                centerOrigin = Position(y = -0.5f)
            ).apply {
                isEditable = true
            }
        }
        return null
    }


    override fun onResume() {
        super.onResume()
        try {
            arSession?.resume()
            sceneView.onSessionResumed
        } catch (e: Exception) {
            Log.e("ArActivity", "Error resuming ARSession: ${e.message}")
           }
    }

    override fun onPause() {
        super.onPause()
        try {
            sceneView.onSessionPaused
            arSession?.pause()
        } catch (e: Exception) {
            Log.e("ArActivity", "Error pausing ARSession: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            sceneView.destroy()
            arSession?.close()
            arSession = null
        } catch (e: Exception) {
            Log.e("ArActivity", "Error destroying ARSession: ${e.message}")
        }
    }

}