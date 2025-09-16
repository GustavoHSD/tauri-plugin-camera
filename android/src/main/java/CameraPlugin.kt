package app.tauri.camera

import android.app.Activity
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import androidx.activity.result.ActivityResult
import app.tauri.annotation.ActivityCallback
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import java.io.File
import java.io.FileInputStream

@TauriPlugin
class CameraPlugin(private val activity: Activity) : Plugin(activity) {
    private lateinit var webView: WebView

    override fun load(webView: WebView) {
        Log.w("CameraPlugin", "load called")
        Log.w("CameraPlugin", "|")
        Log.w("CameraPlugin", "|")
        Log.w("CameraPlugin", "|")
        Log.w("CameraPlugin", "|")
        Log.w("CameraPlugin", "|")
        Log.e("CameraPlugin", "Name: ${activity.localClassName}")
        super.load(webView)
        this.webView = webView
    }

    @Command
    fun takePicture(invoke: Invoke) {
        Log.w("CameraPlugin", "takePicture called")

        val intent = Intent(activity, CameraActivity::class.java).apply {
            putExtra("mode", "takePicture") // Add mode for taking a picture
        }
        startActivityForResult(invoke, intent, "onPictureTaken")
    }

    @ActivityCallback
    private fun onPictureTaken(invoke: Invoke, result: ActivityResult) {
        Log.w("CameraPlugin", "onPictureTaken called")
        Log.w("CameraPlugin", "result.resultCode: " + result.resultCode + " Activity.RESULT_OK: " + Activity.RESULT_OK)
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra("imagePath")
            Log.w("CameraPlugin", "result.data?.getStringExtra('imagePath'): " + imagePath)
            if (imagePath != null) {
                try {
                    val file = File(imagePath)
                    val bytes = FileInputStream(file).use { it.readBytes() }
                    //val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                    val width = result.data?.getIntExtra("width", -1)
                    val height = result.data?.getIntExtra("height", -1)

                    val returnObj = JSObject()
                    returnObj.put("imageData", bytes.toString())
                    returnObj.put("width", width)
                    returnObj.put("height", height)

                    Log.w("CameraPlugin", "Returning imageData as base64, length: ${bytes.size}")
                    Log.w("CameraPlugin", "Width: ${width}")
                    Log.w("CameraPlugin", "Height: ${height}")
                    invoke.resolve(returnObj)
                } catch (e: Exception) {
                    Log.e("CameraPlugin", "Failed to read image file: $e")
                    invoke.reject("Failed to read image file")
                }
            } else {
                Log.w("CameraPlugin", "result.data?.getStringExtra('videoPath'): " + result.data?.getStringExtra("videoPath"))
                invoke.reject("Failed to capture image")
            }
        } else {
            invoke.reject("Camera operation canceled")
        }
    }

    @Command
    fun recordVideo(invoke: Invoke) {
        val intent = Intent(activity, CameraActivity::class.java).apply {
            putExtra("mode", "recordVideo") // Add mode for recording a video
        }
        startActivityForResult(invoke, intent, "onVideoRecorded")
    }

    @ActivityCallback
    private fun onVideoRecorded(invoke: Invoke, result: ActivityResult) {
        Log.w("CameraPlugin", "onVideoRecorded called")
        Log.w("CameraPlugin", "result.resultCode: " + result.resultCode + " Activity.RESULT_OK: " + Activity.RESULT_OK)
        if (result.resultCode == Activity.RESULT_OK) {
            val videoPath = result.data?.getStringExtra("videoPath")
            Log.w("CameraPlugin", "result.data?.getStringExtra('videoPath'): " + videoPath)
            if (videoPath != null) {
                try {
                    val file = File(videoPath)
                    val bytes = FileInputStream(file).use { it.readBytes() }
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                    val width = result.data?.getIntExtra("width", -1)
                    val height = result.data?.getIntExtra("height", -1)

                    val returnObj = JSObject()
                    returnObj.put("videoData", base64)
                    returnObj.put("width", width)
                    returnObj.put("height", height)

                    Log.w("CameraPlugin", "Width: ${width}")
                    Log.w("CameraPlugin", "Height: ${height}")
                    Log.w("CameraPlugin", "Returning videoData as base64, length: ${base64.length}")
                    invoke.resolve(returnObj)
                } catch (e: Exception) {
                    Log.e("CameraPlugin", "Failed to read video file: $e")
                    invoke.reject("Failed to read video file")
                }
            } else {
                Log.w("CameraPlugin", "result.data?.getStringExtra('videoPath'): " + result.data?.getStringExtra("videoPath"))
                invoke.reject("Failed to record video")
            }
        } else {
            invoke.reject("Video recording canceled")
        }
    }
}