package app.tauri.camera

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.WebView
import androidx.activity.result.ActivityResult
import app.tauri.annotation.ActivityCallback
import app.tauri.annotation.Command
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin

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
                val returnObj = JSObject()
                returnObj.put("imagePath", imagePath)
                Log.w("CameraPlugin", "Vamos chamar o invoke.resolve() " + imagePath)
                invoke.resolve(returnObj)
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
                val returnObj = JSObject()
                returnObj.put("videoPath", videoPath)
                Log.w("CameraPlugin", "Vamos chamar o invoke.resolve() " + videoPath)
                invoke.resolve(returnObj)
            } else {
                Log.w("CameraPlugin", "result.data?.getStringExtra('videoPath'): " + result.data?.getStringExtra("videoPath"))
                invoke.reject("Failed to record video")
            }
        } else {
            invoke.reject("Video recording canceled")
        }
    }
}