package app.tauri.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnTakePicture: ImageButton
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var btnLeaveCam: ImageButton
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

        // Calculate bottom bar height (about 18% of screen height)
        val displayMetrics = resources.displayMetrics
        val bottomBarHeight = (displayMetrics.heightPixels * 0.18).toInt()

        // Root layout
        rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setBackgroundColor(Color.BLACK)
        }

        // Camera preview area (above bottom bar)
        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                MATCH_PARENT,
                displayMetrics.heightPixels - bottomBarHeight
            ).apply {
                gravity = Gravity.TOP
            }
        }
        rootLayout.addView(previewView)

        // Bottom bar (white, fixed height)
        val bottomBar = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                MATCH_PARENT,
                bottomBarHeight
            ).apply {
                gravity = Gravity.BOTTOM
            }
            setBackgroundColor(Color.WHITE)
        }

        // Switch camera button (left)
        btnSwitchCamera = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(100, 100).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                marginStart = 48
            }
            setImageResource(android.R.drawable.ic_menu_camera) // Replace with a better icon if available
            setColorFilter(Color.parseColor("#2196F3"))
            background = null
        }
        bottomBar.addView(btnSwitchCamera)

        // Modern capture button (center)
        btnTakePicture = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(140, 140).apply {
                gravity = Gravity.CENTER
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#4EE3E6")) // Light teal
            }
            setImageResource(0)
        }
        bottomBar.addView(btnTakePicture)

        // Modern record video button (center, hidden by default unless in video mode)
        btnRecordVideo = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(140, 140).apply {
                gravity = Gravity.CENTER
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.GREEN)
            }
            setImageResource(0)
            visibility = View.GONE // Only show in video mode
        }
        bottomBar.addView(btnRecordVideo)

        // Leave camera button (right, optional)
        btnLeaveCam = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(100, 100).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                marginEnd = 48
            }
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.parseColor("#2196F3"))
            background = null
        }
        bottomBar.addView(btnLeaveCam)

        // Add bottom bar to root layout
        rootLayout.addView(bottomBar)

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
        btnLeaveCam.setOnClickListener { finishActivity() }
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
        // Preview layout: 90% of screen, white background, rounded corners
        val previewLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#CCFFFFFF"))
        }

        // Calculate bottom bar height (about 10% of screen height)
        val displayMetrics = resources.displayMetrics
        val bottomBarHeight = (displayMetrics.heightPixels * 0.10).toInt()

        val previewContainer = FrameLayout(this).apply {
            val size = (displayMetrics.heightPixels * 0.9).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.WHITE)
                cornerRadius = 48f
            }
            elevation = 16f
        }

        // Add an ImageView or VideoView for the preview
        if (isVideo) {
            val videoView = VideoView(this).apply {
                setVideoURI(Uri.fromFile(file))
                layoutParams = FrameLayout.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT,
                    Gravity.CENTER
                )
                // Attach native controls
                val mediaController = MediaController(this@CameraActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT,
                        Gravity.CENTER
                    ).apply {
                        // position the media controller right above bottom bar
                        val isPortrait = resources.configuration.orientation == 1
                        if (isPortrait) {
                            bottomMargin = bottomBarHeight + (measuredHeight / 2).toInt()
                        }
                    }
                }
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
            }
            previewContainer.addView(videoView)
            // Start video as soon as possible and again after layout
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                videoView.start()
            }
            videoView.requestFocus()
            previewContainer.post {
                videoView.start()
            }
        } else {
            val imageView = ImageView(this).apply {
                setImageURI(Uri.fromFile(file))
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.WHITE)
            }
            previewContainer.addView(imageView)
        }
        previewLayout.addView(previewContainer)

        // Bottom bar (white, fixed height, overlays preview)
        val bottomBar = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                MATCH_PARENT,
                bottomBarHeight
            ).apply {
                gravity = Gravity.BOTTOM
            }
            setBackgroundColor(Color.WHITE)
            elevation = 32f
        }

        // Save Button (left)
        val btnAccept = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                marginStart = 80
            }
            setImageResource(android.R.drawable.checkbox_on_background)
            setColorFilter(Color.parseColor("#2196F3"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke(4, Color.parseColor("#2196F3"))
            }
            visibility = View.VISIBLE
            alpha = 1.0f
            setOnClickListener {
                val intent = Intent().putExtra(if (isVideo) "videoPath" else "imagePath", file.absolutePath)
                filePath = file.absolutePath
                finishActivity(intent)
            }
        }
        bottomBar.addView(btnAccept)

        // Cancel Button (right)
        val btnCancel = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                marginEnd = 80
            }
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.RED)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke(4, Color.RED)
            }
            visibility = View.VISIBLE
            alpha = 1.0f
            setOnClickListener {
                file.delete()
                setContentView(rootLayout)
                setupCamera()
            }
        }
        bottomBar.addView(btnCancel)

        previewLayout.addView(bottomBar)

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

    @RequiresPermission(allOf = [Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO])
    private fun toggleRecording() {
        if (recording != null) {
            recording?.stop()
            recording = null
            // Set button color to green (idle)
            (btnRecordVideo.background as? GradientDrawable)?.setColor(Color.GREEN)
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
                            // Set button color to red (recording)
                            (btnRecordVideo.background as? GradientDrawable)?.setColor(Color.RED)
                        }
                        is VideoRecordEvent.Finalize -> {
                            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
                            // Set button color to green (idle)
                            (btnRecordVideo.background as? GradientDrawable)?.setColor(Color.GREEN)
                            if (!event.hasError()) {
                                showPreview(videoFile, isVideo = true)
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
