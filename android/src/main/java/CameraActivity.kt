package app.tauri.camera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.VideoCapture.OnVideoSavedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnTakePicture: ImageButton
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var btnRecordVideo: ImageButton

    private lateinit var rootLayout: FrameLayout

    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private var filePath = ""

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            setupCamera()
        } else {
            Toast.makeText(this, "Camera and audio permissions are required", Toast.LENGTH_SHORT).show()
            finishActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePath = ""

        // Create a root layout
        rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add PreviewView
        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootLayout.addView(previewView)

        // Add Take Picture Button
        btnTakePicture = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 50
            }
        }
        rootLayout.addView(btnTakePicture)

        // Add Record Video Button
        btnRecordVideo = ImageButton(this).apply {
            setImageResource(android.R.drawable.presence_video_online)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = 50
                marginEnd = 50
            }
        }
        rootLayout.addView(btnRecordVideo)

        // Add Switch Camera Button
        btnSwitchCamera = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 50
                marginEnd = 50
            }
        }
        rootLayout.addView(btnSwitchCamera)

        // Add Flash Button
        btnFlash = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_manage)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = 50
                marginStart = 50
            }
        }
        rootLayout.addView(btnFlash)

        // Set the root layout as the content view
        setContentView(rootLayout)

        // Check and request permissions
        if (hasPermissions()) {
            setupCamera()
        } else {
            permissionsLauncher.launch(permissions)
        }

        // Get mode from intent and configure UI
        val mode = intent.getStringExtra("mode")
        configureUIForMode(mode)

        // Set button listeners
        btnTakePicture.setOnClickListener { takePicture() }
        btnSwitchCamera.setOnClickListener { toggleCamera() }
        btnFlash.setOnClickListener { toggleFlash() }
        btnRecordVideo.setOnClickListener { toggleRecording() }
    }

    @JvmOverloads
    fun finishActivity(intent: Intent? = Intent()) {
        if (filePath.isNotEmpty()) {
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED, intent)
        }
        finish()
    }

    private fun configureUIForMode(mode: String?) {
        when (mode) {
            "takePicture" -> {
                btnTakePicture.visibility = View.VISIBLE
                btnRecordVideo.visibility = View.GONE
            }
            "recordVideo" -> {
                btnTakePicture.visibility = View.GONE
                btnRecordVideo.visibility = View.VISIBLE
            }
            else -> {
                btnTakePicture.visibility = View.VISIBLE
                btnRecordVideo.visibility = View.VISIBLE
            }
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                bindPreview(
                    cameraProvider,
                    if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to bind camera use cases", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider, cameraDirection: Int) {
        val executor = ContextCompat.getMainExecutor(this)

       try {
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraDirection)
                .build()

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to bind camera use cases: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPreview(file: File, isVideo: Boolean) {
        // Create a new layout for the preview
        val previewLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add an ImageView or VideoView for the preview
        if (isVideo) {
            val videoView = VideoView(this).apply {
                setVideoURI(Uri.fromFile(file))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                start()
            }
            previewLayout.addView(videoView)
        } else {
            val imageView = ImageView(this).apply {
                setImageURI(Uri.fromFile(file))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            previewLayout.addView(imageView)
        }

        // Add Accept Button
        val btnAccept = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_save)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = 50
                marginEnd = 50
            }
            setOnClickListener {
                Log.w("=======================", "Accept button clicked")
                val intent = Intent().putExtra(if (isVideo) "videoPath" else "imagePath", file.absolutePath)
                filePath = file.absolutePath
                Log.w("=======================", "Activity.setResult called with OK result")
                finishActivity(intent)
            }
        }
        previewLayout.addView(btnAccept)

        // Add Cancel Button
        val btnCancel = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                bottomMargin = 50
                marginStart = 50
            }
            setOnClickListener {
                Log.w("=======================", "Cancel button clicked")
                file.delete()
                setContentView(rootLayout) // Return to the camera view
                setupCamera() // Reinitialize the camera
                Log.w("=======================", "Activity.setResult called with OK result")
            }
        }
        previewLayout.addView(btnCancel)

        // Set the preview layout as the content view
        setContentView(previewLayout)
    }

    private fun takePicture() {
        val photoFile = File(
            cacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    showPreview(photoFile, isVideo = false) // Show preview after saving
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        setupCamera()
    }

    private fun toggleFlash() {
        // Implement flash toggle logic
    }

    @RequiresPermission(allOf = [Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO])
    private fun toggleRecording() {
        if (recording != null) {
            recording?.stop()
            recording = null
        } else {
            val videoFile = File(
                cacheDir,
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".mp4"
            )

            val fileOutputOptions = FileOutputOptions.Builder(videoFile).build()

            recording = videoCapture?.output
                ?.prepareRecording(this, fileOutputOptions)
                ?.withAudioEnabled()
                ?.start(ContextCompat.getMainExecutor(this)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
                        }
                        is VideoRecordEvent.Finalize -> {
                            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
                            if (!event.hasError()) {
                                showPreview(videoFile, isVideo = true) // Show preview after recording
                            } else {
                                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }

    private fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
