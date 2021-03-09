package com.merseyside.ar.sample.base

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.merseyside.ar.R
import com.merseyside.ar.helpers.DisplayRotationHelper
import com.merseyside.ar.helpers.TrackingStateHelper
import com.merseyside.ar.rendering.BackgroundRenderer
import com.merseyside.ar.rendering.ObjectRenderer
import com.merseyside.ar.rendering.PlaneAttachment
import com.merseyside.ar.rendering.PlaneRenderer
import com.merseyside.ar.sample.utils.CameraPermissionHelper
import com.merseyside.archy.presentation.activity.BaseBindingActivity
import com.merseyside.utils.ext.log
import com.merseyside.utils.ext.logMsg
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class ArActivity<B : ViewDataBinding> : BaseBindingActivity<B>(), GLSurfaceView.Renderer {

    private var mUserRequestedInstall = true
    private var session: Session? = null
    private lateinit var gestureDetector: GestureDetector
    private lateinit var displayRotationHelper: DisplayRotationHelper

    private val maxAllocationSize = 16
    private val anchorMatrix = FloatArray(maxAllocationSize)
    private val queuedSingleTaps = ArrayBlockingQueue<MotionEvent>(maxAllocationSize)
    private lateinit var trackingStateHelper: TrackingStateHelper

    private val backgroundRenderer: BackgroundRenderer = BackgroundRenderer()
    private val planeRenderer: PlaneRenderer = PlaneRenderer()

    private val surfaceView: GLSurfaceView by lazy { findViewById(getSurfaceViewId()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayRotationHelper = DisplayRotationHelper(this)
        trackingStateHelper = TrackingStateHelper(this)

        setupTapDetector()
        setupSurfaceView()
    }

    override fun onResume() {
        super.onResume()
        if (checkCameraPermission()) {
            installAr()
        }

        surfaceView.onResume()
        session?.resume()
        displayRotationHelper.onResume()
    }

    override fun onPause() {
        super.onPause()

        surfaceView.onPause()
        session?.pause()
        displayRotationHelper.onPause()
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
        surfaceView.let {
            it.preserveEGLContextOnPause = true
            it.setEGLContextClientVersion(2)
            it.setEGLConfigChooser(8, 8, 8, 8, maxAllocationSize, 0)
            it.setRenderer(this)
            it.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            it.setWillNotDraw(false)
            it.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
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
                e.x.log()
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
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        logMsg("Surface created")
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)


        session?.let {
            // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
            try {
                // Create the texture and pass it to ARCore session to be filled during update().
                backgroundRenderer.createOnGlThread(this)
                planeRenderer.createOnGlThread(this, getString(R.string.model_grid_png))

                onSurfaceCreated(session!!)

            } catch (e: IOException) {
                Log.e(TAG, getString(R.string.failed_to_read_asset), e)
            }
        }
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        displayRotationHelper.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        session?.let {
            // Notify ARCore session that the view size changed
            displayRotationHelper.updateSessionIfNeeded(it)

            try {
                it.setCameraTextureName(backgroundRenderer.textureId)

                val frame = it.update()
                val camera = frame.camera
//
                handleTap(frame, camera)
                drawBackground(frame)
//
                trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)
//
                onDrawFrame(camera, frame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun drawBackground(frame: Frame) {
        backgroundRenderer.draw(frame)
    }

    private fun handleTap(frame: Frame, camera: Camera) {
        val tap = queuedSingleTaps.poll()

        if (tap != null && camera.trackingState == TrackingState.TRACKING) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            onTap(camera, frame, frame.hitTest(tap))
        }
    }

    protected fun isValidPlane(camera: Camera, hit: HitResult): Boolean {
        val trackable = hit.trackable

        return trackable is Plane
                && trackable.isPoseInPolygon(hit.hitPose)
                && PlaneRenderer.calculateDistanceToPlane(hit.hitPose, camera.pose) > 0
    }

    fun computeProjectionMatrix(camera: Camera): FloatArray {
        val projectionMatrix = FloatArray(maxAllocationSize)
        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

        return projectionMatrix
    }

    fun computeViewMatrix(camera: Camera): FloatArray {
        val viewMatrix = FloatArray(maxAllocationSize)
        camera.getViewMatrix(viewMatrix, 0)

        return viewMatrix
    }

    /**
     * Compute lighting from average intensity of the image.
     */
    fun computeLightIntensity(frame: Frame): FloatArray {
        val lightIntensity = FloatArray(4)
        frame.lightEstimate.getColorCorrection(lightIntensity, 0)

        return lightIntensity
    }

    /**
     *  Visualizes planes.
     */
    fun visualizePlanes(camera: Camera, projectionMatrix: FloatArray) {
        planeRenderer.drawPlanes(
            session!!.getAllTrackables(Plane::class.java),
            camera.displayOrientedPose,
            projectionMatrix
        )
    }

    fun addSessionAnchorFromAttachment(
        previousAttachment: PlaneAttachment?,
        hit: HitResult
    ): PlaneAttachment {
        // 1
        previousAttachment?.anchor?.detach()

        // 2
        val plane = hit.trackable as Plane
        val pose = hit.hitPose
        val anchor = session!!.createAnchor(pose)

        // 3
        return PlaneAttachment(plane, anchor)
    }

    fun drawObject(
        objectRenderer: ObjectRenderer,
        planeAttachment: PlaneAttachment?,
        scaleFactor: Float,
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray,
        lightIntensity: FloatArray
    ) {
        if (planeAttachment?.isTracking == true) {
            planeAttachment.pose.toMatrix(anchorMatrix, 0)

            // Update and draw the model
            objectRenderer.updateModelMatrix(anchorMatrix, scaleFactor)
            objectRenderer.draw(viewMatrix, projectionMatrix, lightIntensity)
        }
    }


    abstract fun getSurfaceViewId(): Int
    abstract fun onSurfaceCreated(session: Session)
    abstract fun onDrawFrame(camera: Camera, frame: Frame)
    abstract fun onTap(camera: Camera, frame: Frame, hitResults: List<HitResult>)

    companion object {
        private const val TAG = "ArActivity"
    }
}
