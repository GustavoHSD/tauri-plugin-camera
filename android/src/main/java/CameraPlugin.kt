package app.tauri.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke

@TauriPlugin
class CameraPlugin(private val activity: Activity) : Plugin(activity) {
    private val cameraHandler = CameraHandler(activity)

    @Command
    fun takePicture(invoke: Invoke) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        invoke.resolve(cameraHandler.takePicture(intent))
    }

    @Command
    fun recordVideo(invoke: Invoke) {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        invoke.resolve(cameraHandler.recordVideo(intent))
    }
}

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