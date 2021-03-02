package com.merseyside.ar.sample.base

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.google.ar.core.ArCoreApk
import com.google.ar.core.R
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.merseyside.ar.rendering.BackgroundRenderer
import com.merseyside.ar.rendering.PlaneRenderer
import com.merseyside.ar.sample.utils.CameraPermissionHelper
import com.merseyside.archy.presentation.activity.BaseBindingActivity
import com.merseyside.utils.ext.logMsg
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class ArActivity<B : ViewDataBinding> : BaseBindingActivity<B>(), GLSurfaceView.Renderer {

    private var mUserRequestedInstall = true
    private var session: Session? = null
    private lateinit var gestureDetector: GestureDetector

    private val maxAllocationSize = 16
    private val anchorMatrix = FloatArray(maxAllocationSize)
    private val queuedSingleTaps = ArrayBlockingQueue<MotionEvent>(maxAllocationSize)

    private val backgroundRenderer: BackgroundRenderer = BackgroundRenderer()
    private val planeRenderer: PlaneRenderer = PlaneRenderer()

    private val surfaceView: GLSurfaceView by lazy { findViewById(getSurfaceViewId()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupTapDetector()
    }

    override fun onResume() {
        super.onResume()
        if (checkCameraPermission()) {
            installAr()
        }
    }

    private fun checkArAvailability() = ArCoreApk.getInstance().checkAvailability(this)

    private fun installAr() {
        try {
            if (session == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // Success: Safe to create the AR session.
                        session = Session(this)
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        // When this method returns `INSTALL_REQUESTED`:
                        // 1. ARCore pauses this activity.
                        // 2. ARCore prompts the user to install or update Google Play
                        //    Services for AR (market://details?id=com.google.ar.core).
                        // 3. ARCore downloads the latest device profile data.
                        // 4. ARCore resumes this activity. The next invocation of
                        //    requestInstall() will either return `INSTALLED` or throw an
                        //    exception if the installation or update did not succeed.
                        mUserRequestedInstall = false
                        return
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception $e", Toast.LENGTH_LONG)
                .show()
            return
        }
    }

    private fun setupSurfaceView() {
        // Set up renderer.
        surfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(2)
            setEGLConfigChooser(8, 8, 8, 8, maxAllocationSize, 0)
            setRenderer(this@ArActivity)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            setWillNotDraw(false)
            setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            false
        } else {
            true
        }
    }

    private fun setupTapDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onSingleTap(e)
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        })
    }

    private fun onSingleTap(e: MotionEvent) {
        // Queue tap if there is space. Tap is lost if queue is full.
        queuedSingleTaps.offer(e)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(this)
            planeRenderer.createOnGlThread(this, getString(R.string.model_grid_png))

            // TODO - set up the objects
            // 1
            vikingObject.createOnGlThread(this@MainActivity, getString(R.string.model_viking_obj), getString(
                R.string.model_viking_png))
            cannonObject.createOnGlThread(this@MainActivity, getString(R.string.model_cannon_obj), getString(
                R.string.model_cannon_png))
            targetObject.createOnGlThread(this@MainActivity, getString(R.string.model_target_obj), getString(
                R.string.model_target_png))

            // 2
            targetObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
            vikingObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
            cannonObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)

        } catch (e: IOException) {
            Log.e(TAG, getString(R.string.failed_to_read_asset), e)
        }
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onDrawFrame(p0: GL10?) {
        TODO("Not yet implemented")
    }

    abstract fun getSurfaceViewId(): Int
}
