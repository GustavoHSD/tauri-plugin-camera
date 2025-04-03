package app.tauri.camera

import android.content.Context
import android.hardware.Camera
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.IOException

class CameraHandler(private val context: Context) {
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null

    fun openCamera(): Boolean {
        return try {
            camera = Camera.open()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun takePicture(): Uri? {
        val pictureFile = createImageFile() ?: return null
        camera?.takePicture(null, null) { _, data ->
            try {
                pictureFile.writeBytes(data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Uri.fromFile(pictureFile)
    }

    fun startRecordingVideo(): Boolean {
        mediaRecorder = MediaRecorder()
        camera?.unlock()
        mediaRecorder?.setCamera(camera)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(createVideoFile()?.absolutePath)
        mediaRecorder?.setPreviewDisplay(null) // Set a SurfaceView or TextureView for preview
        return try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopRecordingVideo() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        camera?.lock()
    }

    fun releaseCamera() {
        camera?.release()
        camera = null
    }

    private fun createImageFile(): File? {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_", ".jpg", storageDir)
    }

    private fun createVideoFile(): File? {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile("VID_", ".mp4", storageDir)
    }
}