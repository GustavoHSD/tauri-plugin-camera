package app.tauri.camera

import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import androidx.activity.result.ActivityResult
import app.tauri.annotation.ActivityCallback
//import android.net.Uri
import android.provider.MediaStore
import app.tauri.annotation.Command
import app.tauri.plugin.Invoke
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin

@TauriPlugin
class CameraPlugin(private val activity: Activity) : Plugin(activity) {
    private lateinit var webView: WebView
    private val cameraHandler = CameraHandler(activity)

    override fun load(webView: WebView) {
        super.load(webView)
        this.webView = webView
    }

    @Command
    fun takePicture(invoke: Invoke) {
        val intent = Intent(activity, CameraActivity::class.java)
        startActivityForResult(invoke, intent, "onPictureTaken")
    }

    @ActivityCallback
    private fun onPictureTaken(invoke: Invoke, result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra("imagePath")
            if (imagePath != null) {
                val returnObj = JSObject()
                returnObj.put("imagePath", imagePath)
                invoke.resolve(returnObj)
                //invoke.resolve(mapOf("imagePath" to imagePath))
            } else {
                invoke.reject("Failed to capture image")
            }
        } else {
            invoke.reject("Camera operation canceled")
        }
    }

    @Command
    fun recordVideo(invoke: Invoke) {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        invoke.resolve(cameraHandler.recordVideo(intent))
    }
}