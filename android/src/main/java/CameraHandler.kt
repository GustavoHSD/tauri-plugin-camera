package app.tauri.camera

//import android.content.Context
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore

import android.hardware.Camera
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.IOException

import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
// class CameraHandler(private val context: Context) {
//     private var camera: Camera? = null
//     private var mediaRecorder: MediaRecorder? = null

//     fun openCamera(): Boolean {
//         return try {
//             camera = Camera.open()
//             true
//         } catch (e: Exception) {
//             false
//         }
//     }

//     fun takePicture(): Uri? {
//         val pictureFile = createImageFile() ?: return null
//         camera?.takePicture(null, null) { _, data ->
//             try {
//                 pictureFile.writeBytes(data)
//             } catch (e: IOException) {
//                 e.printStackTrace()
//             }
//         }
//         return Uri.fromFile(pictureFile)
//     }

//     fun startRecordingVideo(): Boolean {
//         mediaRecorder = MediaRecorder()
//         camera?.unlock()
//         mediaRecorder?.setCamera(camera)
//         mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
//         mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
//         mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//         mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//         mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//         mediaRecorder?.setOutputFile(createVideoFile()?.absolutePath)
//         mediaRecorder?.setPreviewDisplay(null) // Set a SurfaceView or TextureView for preview
//         return try {
//             mediaRecorder?.prepare()
//             mediaRecorder?.start()
//             true
//         } catch (e: Exception) {
//             e.printStackTrace()
//             false
//         }
//     }

//     fun stopRecordingVideo() {
//         mediaRecorder?.stop()
//         mediaRecorder?.release()
//         mediaRecorder = null
//         camera?.lock()
//     }

//     fun releaseCamera() {
//         camera?.release()
//         camera = null
//     }

//     private fun createImageFile(): File? {
//         val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//         return File.createTempFile("IMG_", ".jpg", storageDir)
//     }

//     private fun createVideoFile(): File? {
//         val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
//         return File.createTempFile("VID_", ".mp4", storageDir)
//     }
// }

class CameraHandler(private val activity: Activity) {
    fun takePicture(intent: Intent): JSObject {
        // Logic to handle taking a picture
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        // Return a JSObject with the file reference or status
        val result = JSObject()
        result.put("status", "Picture taken")
        return result
    }

    fun recordVideo(intent: Intent): JSObject {
        // Logic to handle recording a video
        activity.startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
        // Return a JSObject with the file reference or status
        val result = JSObject()
        result.put("status", "Video recorded")
        return result
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_VIDEO_CAPTURE = 2
    }
}