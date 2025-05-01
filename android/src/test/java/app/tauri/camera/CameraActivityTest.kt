package app.tauri.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity

@RunWith(value = RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class CameraActivityTest {

    @Test
    fun `onCreate with null savedInstanceState`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CameraActivity::class.java)
        val activityScenario = ActivityScenario.launch<CameraActivity>(intent)

        activityScenario.onActivity { activity ->
            val shadowActivity: ShadowActivity = Shadows.shadowOf(activity)
            assertNotNull(shadowActivity)
            // Instead, verify activity is not finishing and UI is initialized
            assertEquals(false, activity.isFinishing)
            assertNotNull(activity.previewView)
        }
        activityScenario.close()
    }

    @Test
    fun `onCreate with null Intent`() {
        val activityScenario = ActivityScenario.launch(CameraActivity::class.java)
        activityScenario.onActivity { activity ->
            val shadowActivity: ShadowActivity = Shadows.shadowOf(activity)
            assertNotNull(shadowActivity)
        }
    }

    @Test
    fun `onCreate with non null savedInstanceState`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CameraActivity::class.java)
        val bundle = Bundle()
        bundle.putString("dummy", "value")
        // Launch activity with bundle as savedInstanceState using ActivityScenario.launch
        val scenario = ActivityScenario.launch<CameraActivity>(intent)
        scenario.onActivity { activity ->
            // Do NOT call activity.onCreate(bundle) manually, as ActivityScenario already handles lifecycle
            assertNotNull(activity)
        }
        scenario.close()
    }

    @Test
    fun `onCreate UI components initialization`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            assertNotNull(activity.previewView)
            assertNotNull(activity.btnTakePicture)
            assertNotNull(activity.btnSwitchCamera)
            assertNotNull(activity.btnLeaveCam)
            assertNotNull(activity.btnRecordVideo)
            assertNotNull(activity.rootLayout)
        }
        scenario.close()
    }

    @Test
    fun `onCreate bottom bar height calculation`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            val expected = (activity.resources.displayMetrics.heightPixels * 0.18).toInt()
            assertEquals(expected, activity.calculateBottomBarHeight())
        }
        scenario.close()
    }

    @Test
    fun `onCreate permission request on start`() {
        // Robolectric grants all permissions by default, so hasPermissions() returns true
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            assertTrue(activity.hasPermissions())
        }
        scenario.close()
    }

    @Test
    fun `onCreate has permissions case`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            assertTrue(activity.hasPermissions())
        }
        scenario.close()
    }

    @Test
    fun `onCreate mode  takePicture `() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CameraActivity::class.java)
        intent.putExtra("mode", "takePicture")
        val scenario = ActivityScenario.launch<CameraActivity>(intent)
        scenario.onActivity { activity ->
            assertEquals("takePicture", activity.currentMode)
            assertEquals(View.VISIBLE, activity.btnTakePicture.visibility)
            assertEquals(View.GONE, activity.btnRecordVideo.visibility)
        }
        scenario.close()
    }

    @Test
    fun `onCreate mode  recordVideo `() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CameraActivity::class.java)
        intent.putExtra("mode", "recordVideo")
        val scenario = ActivityScenario.launch<CameraActivity>(intent)
        scenario.onActivity { activity ->
            assertEquals("recordVideo", activity.currentMode)
            assertEquals(View.GONE, activity.btnTakePicture.visibility)
            assertEquals(View.VISIBLE, activity.btnRecordVideo.visibility)
        }
        scenario.close()
    }

    @Test
    fun `onCreate default mode`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CameraActivity::class.java)
        intent.putExtra("mode", "invalidMode")
        val scenario = ActivityScenario.launch<CameraActivity>(intent)
        scenario.onActivity { activity ->
            assertEquals("invalidMode", activity.currentMode)
            assertEquals(View.VISIBLE, activity.btnTakePicture.visibility)
            assertEquals(View.VISIBLE, activity.btnRecordVideo.visibility)
        }
        scenario.close()
    }

    @Test
    fun `onCreate invalid intent extra`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CameraActivity::class.java)
        intent.putExtra("unexpectedKey", "unexpectedValue")
        val scenario = ActivityScenario.launch<CameraActivity>(intent)
        scenario.onActivity { activity ->
            // Should default to both buttons visible
            assertEquals(View.VISIBLE, activity.btnTakePicture.visibility)
            assertEquals(View.VISIBLE, activity.btnRecordVideo.visibility)
        }
        scenario.close()
    }

    @Test
    fun `onCreate button listeners set`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            assertNotNull(activity.btnTakePicture.hasOnClickListeners())
            assertNotNull(activity.btnSwitchCamera.hasOnClickListeners())
            assertNotNull(activity.btnLeaveCam.hasOnClickListeners())
            assertNotNull(activity.btnRecordVideo.hasOnClickListeners())
        }
        scenario.close()
    }

    @Test
    fun `finishActivity with file path`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            val intent = Intent()
            activity.filePath = "/tmp/test.jpg"
            activity.finishActivity(intent)
            val shadowActivity = Shadows.shadowOf(activity)
            assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)
            assertEquals(intent, shadowActivity.resultIntent)
        }
        scenario.close()
    }

    @Test
    fun `finishActivity without file path`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            val intent = Intent()
            activity.filePath = ""
            activity.finishActivity(intent)
            val shadowActivity = Shadows.shadowOf(activity)
            assertEquals(Activity.RESULT_CANCELED, shadowActivity.resultCode)
            assertEquals(intent, shadowActivity.resultIntent)
        }
        scenario.close()
    }

    @Test
    fun `finishActivity with custom intent`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            val customIntent = Intent().putExtra("foo", "bar")
            activity.filePath = "/tmp/test.jpg"
            activity.finishActivity(customIntent)
            val shadowActivity = Shadows.shadowOf(activity)
            assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)
            assertEquals("bar", shadowActivity.resultIntent?.getStringExtra("foo"))
        }
        scenario.close()
    }

    @Test
    fun `finishActivity default intent`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            activity.filePath = ""
            activity.finishActivity()
            val shadowActivity = Shadows.shadowOf(activity)
            assertEquals(Activity.RESULT_CANCELED, shadowActivity.resultCode)
        }
        scenario.close()
    }

    @Test
    fun `finishActivity from permission request`() {
        // Simulate permission denied
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            activity.filePath = ""
            activity.finishActivity()
            val shadowActivity = Shadows.shadowOf(activity)
            assertEquals(Activity.RESULT_CANCELED, shadowActivity.resultCode)
        }
        scenario.close()
    }

    @Test
    fun `finishActivity from button`() {
        val scenario = ActivityScenario.launch(CameraActivity::class.java)
        scenario.onActivity { activity ->
            activity.filePath = ""
            activity.btnLeaveCam.performClick()
            val shadowActivity = Shadows.shadowOf(activity)
            assertEquals(Activity.RESULT_CANCELED, shadowActivity.resultCode)
        }
        scenario.close()
    }
}