package com.example.presence.face_recognization

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.presence.R
import com.example.presence.authentication.LoginActivity
import com.example.presence.bottom_navigation.Home
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.android.synthetic.main.add_task_dialog.*
import kotlinx.android.synthetic.main.alert.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.acos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

private var mContext: Context? = null
class Liveness : AppCompatActivity() {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build()
    )
    private var state: State = State.IDLE
    private var lastFaceCount: Int = 0
    private var lastFace: Face? = null
    private val detectionRect = Rect()

    private var facingStartTime = 0L
    private var facingEulerAngleY = 0f
    private var detectNoFaceTimes = 0

    @SuppressLint("SetTextI18n")
    private val mlKitAnalyzerConsumer = Consumer<MlKitAnalyzer.Result> { result ->
        if (state == State.COMPLETED) return@Consumer
        val faces = result.getValue(faceDetector)
        val faceCount = faces?.size ?: 0
        val face = faces?.firstOrNull() ?: lastFace
        if (faceCount == 0) {
            detectNoFaceTimes++
        } else {
            lastFace = face
        }
        if (detectNoFaceTimes > ALLOW_NO_FACE_TIMES) {

            val dialog = Dialog(mContext!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.alert)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            dialog.header.setText("Proxy Detected")
            dialog.text.setText("You are marking the proxy of the another person which is not allowed")

            dialog.dialog_ok.setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this@Liveness, LoginActivity::class.java))
                finish()
            }
            lastFace = null
            detectNoFaceTimes = 0
            state = State.IDLE
        } else if (faceCount > 1) {
            state = State.IDLE
            if (lastFaceCount <= 1) {
                Toast.makeText(
                    this,
                    "Please make sure there is only one face on the screen.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (!isFaceInDetectionRect(face)) {
            state = State.IDLE
        }
        lastFaceCount = faceCount

        when (state) {
            State.IDLE -> {
                findViewById<TextView>(R.id.guide).text = "Please squarely facing the camera"
                if (isFacingCamera(face)) {
                    state = State.START_FACING
                }
            }
            State.START_FACING -> {
                findViewById<TextView>(R.id.guide).text = "Please squarely facing the camera"
                if (isFacingCamera(face)) {
                    state = State.FACING
                    facingStartTime = System.currentTimeMillis()
                }
            }
            State.FACING -> {
                if (!isFacingCamera(face)) {
                    state = State.START_FACING
                } else if (System.currentTimeMillis() - facingStartTime >= FACING_CAMERA_KEEP_TIME) {
                    state = State.START_SMILE
                    facingEulerAngleY = face?.headEulerAngleY ?: 0f
                }
            }
            State.START_SMILE -> {
                findViewById<TextView>(R.id.guide).text = "Please keep smiling"
                state = State.SMILING
            }
            State.SMILING -> {
                if (isSmiling(face)) {
                    state = State.START_MOUTH
                }
            }
            State.START_MOUTH -> {
                findViewById<TextView>(R.id.guide).text = "Please open your mouth"
                state = State.MOUTH
            }
            State.MOUTH -> {
                if (isMouthOpened(face)) {
                    setResult(RESULT_OK, intent)
                    finish()
                    state = State.COMPLETED
                }
            }
            State.COMPLETED -> {
            }
        }
    }

    private fun isMouthOpened(face: Face?): Boolean {
        face ?: return false
        val left = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position ?: return false
        val right = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position ?: return false
        val bottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position ?: return false
        // Square of lengths be a2, b2, c2
        val a2 = lengthSquare(right, bottom)
        val b2 = lengthSquare(left, bottom)
        val c2 = lengthSquare(left, right)

        // length of sides be a, b, c
        val a = sqrt(a2)
        val b = sqrt(b2)

        // From Cosine law
        val gamma = acos((a2 + b2 - c2) / (2 * a * b))

        // Converting to degrees
        val gammaDeg = gamma * 180 / Math.PI

        return gammaDeg < 110f
    }

    private fun lengthSquare(a: PointF, b: PointF): Float {
        val x = a.x - b.x
        val y = a.y - b.y
        return x * x + y * y
    }

    private fun isSmiling(face: Face?): Boolean {
        return (face?.smilingProbability ?: 0f) > 0.7f
    }

    private fun isFacingCamera(face: Face?): Boolean {
        face ?: return false
        val thresholdX = 20f
        val thresholdY = 12f
        val thresholdZ = 8f
        return face.headEulerAngleZ < thresholdZ && face.headEulerAngleZ > -thresholdZ
                && face.headEulerAngleY < thresholdY && face.headEulerAngleY > -thresholdY
                && face.headEulerAngleX < thresholdX && face.headEulerAngleX > -thresholdX
    }

    private fun isFaceInDetectionRect(face: Face?): Boolean {
        face ?: return false
        val resolutionInfo = imageAnalysis?.resolutionInfo ?: return false
        val resolution = resolutionInfo.resolution
        if (resolution.width < 1 || resolution.height < 1) return false
        val rotateDegrees = resolutionInfo.rotationDegrees
        var resolutionWidth = resolution.width
        var resolutionHeight = resolution.height
        if (rotateDegrees == 90 || rotateDegrees == 270) {
            resolutionWidth = resolution.height
            resolutionHeight = resolution.width
        }
        val faceX = face.boundingBox.centerX()
        val faceY = face.boundingBox.centerY()
        val faceSize = face.boundingBox.width()
        val maxSize = min(resolutionWidth, resolutionHeight) * 1f
        val minSize = min(resolutionWidth, resolutionHeight) * 0.4f
        if (faceSize > maxSize || faceSize < minSize) return false
        detectionRect.set(
            (resolutionWidth * 0.2f).roundToInt(),
            (resolutionHeight * 0.2f).roundToInt(),
            (resolutionWidth * 0.8f).roundToInt(),
            (resolutionHeight * 0.8f).roundToInt()
        )
        return detectionRect.contains(faceX, faceY)
    }

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liveness)
        mContext=this@Liveness

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission deny", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.launch(Manifest.permission.CAMERA)

        findViewById<PreviewView>(R.id.camera_preview).outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, view.height / 2.0f)
            }
        }
        findViewById<PreviewView>(R.id.camera_preview).clipToOutline = true
    }

    @SuppressLint("SetTextI18n")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.camera_preview).surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageAnalysis = ImageAnalysis.Builder().build()
            val mlKitAnalyzer = MlKitAnalyzer(
                listOf(faceDetector),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                ContextCompat.getMainExecutor(this),
                mlKitAnalyzerConsumer
            )
            imageAnalysis?.setAnalyzer(cameraExecutor, mlKitAnalyzer)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }
    override fun onDestroy() {
        super.onDestroy()
        finish()
        cameraExecutor.shutdown()
    }

    private enum class State {
        IDLE,
        START_FACING,
        FACING,
        START_SMILE,
        SMILING,
        START_MOUTH,
        MOUTH,
        COMPLETED
    }

    companion object {
        private const val ALLOW_NO_FACE_TIMES = 10
        private const val FACING_CAMERA_KEEP_TIME = 1000L
    }
}