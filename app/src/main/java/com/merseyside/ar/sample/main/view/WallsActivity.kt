package com.merseyside.ar.sample.main.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Range
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.*
import com.merseyside.animators.BaseAnimator
import com.merseyside.animators.animator.AlphaAnimator
import com.merseyside.ar.R
import com.merseyside.ar.databinding.ActivityWallsBinding
import com.merseyside.ar.rendering.ObjectRenderer
import com.merseyside.ar.rendering.PlaneAttachment
import com.merseyside.ar.rendering.PlaneRenderer
import com.merseyside.ar.sample.base.ArActivity
import com.merseyside.archy.presentation.view.INVISIBLE
import com.merseyside.archy.presentation.view.VISIBLE
import com.merseyside.utils.ext.delay
import com.merseyside.utils.ext.onClick
import com.merseyside.utils.mainThread
import com.merseyside.utils.time.Millis
import com.merseyside.utils.time.Seconds
import com.merseyside.utils.time.TimeUnit
import com.merseyside.utils.time.minus
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class WallsActivity : ArActivity<ActivityWallsBinding>() {
    override fun performInjection(bundle: Bundle?) {}
    override fun getLayoutId() = R.layout.activity_walls
    override fun getToolbar() = null
    override fun getFragmentContainer() = null
    override fun getSurfaceViewId() = getBinding().surfaceView.id

    private var timerJob: Job? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var accelSensor: Sensor
    private lateinit var magneticSensor: Sensor

    private var centerX = 0F
    private var centerY = 0F

    private var animator: BaseAnimator? = null

    private val pointerObject = ObjectRenderer()

    private var pointerAttachment: PlaneAttachment? = null

    private var isAddingPoint: Boolean = false
    private val pointAttachments: MutableList<PlaneAttachment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        centerX = (this.resources.displayMetrics.widthPixels / 2).toFloat()
        centerY = (this.resources.displayMetrics.heightPixels / 2).toFloat()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        getBinding().reset.onClick {
            reset()
        }

        getBinding().putPoint.onClick {
            isAddingPoint = true
        }
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(
            listener,
            accelSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            listener,
            magneticSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(listener)
    }

    // Gravity rotational data
    private var gravity: FloatArray? = null

    // Magnetic rotational data
    private var magnetic: FloatArray? = null //for magnetic rotational data

    private var accels = FloatArray(3)
    private var mags = FloatArray(3)
    private val values = FloatArray(3)

    // azimuth, pitch and roll
    private var pitch = 0f

    private val listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> mags = event.values.clone()
                Sensor.TYPE_ACCELEROMETER -> accels = event.values.clone()
            }

            gravity = FloatArray(9)
            magnetic = FloatArray(9)
            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags)
            val outGravity = FloatArray(9)
            SensorManager.remapCoordinateSystem(
                gravity,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                outGravity
            )
            SensorManager.getOrientation(outGravity, values)

            pitch = values[1] * 57.2957795f

            if (timer.isNotEmpty()) {
                if (timerJob == null) {
                    if (pitch in ANGLE_RANGE) {
                        timerJob = launchTimer()
                        startAnimation()
                    }
                } else if (pitch !in ANGLE_RANGE) {
                    stopTimer()
                    reverseAnimation()
                }
            }
        }
    }

    private fun startAnimation() {
        if (animator == null) {
            animator = AlphaAnimator(AlphaAnimator.Builder(
                view = getBinding().progressBar,
                duration = Millis(500)
            ).apply {
                values(0F, 1F)
            })
        }

        animator!!.start()
    }

    private fun reverseAnimation() {
        animator?.reverse()
    }

    private fun launchTimer(): Job {
        return lifecycleScope.launch {
            while (isActive) {
                updateProgress()
                delay(TIMER_STEP)

                timer -= TIMER_STEP

                if (timer.isEmpty()) {
                    cancel()
                    reverseAnimation()
                    getBinding().reset.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun stopTimer() {
        timer = Seconds(TIMER_VALUE_SECONDS)

        timerJob?.cancel()
        timerJob = null
    }

    private fun updateProgress() {
        getBinding().progressBar.progress = (100 - ((100 * timer.millis)
                / (TIMER_VALUE_SECONDS * 1000)).toInt())
    }

    private fun reset() {
        timer = Seconds(TIMER_VALUE_SECONDS)
        getBinding().reset.visibility = INVISIBLE
    }

    override fun onSurfaceCreated(session: Session) {
        val config = session.config
        config.planeFindingMode = Config.PlaneFindingMode.VERTICAL
        session.configure(config)

        pointerObject.createOnGlThread(
            this, getString(R.string.model_target_obj), getString(
                R.string.model_target_png
            )
        )

        pointerObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
    }

    override fun onDrawFrame(camera: Camera, frame: Frame) {
        val projectionMatrix = computeProjectionMatrix(camera)
        val viewMatrix = computeViewMatrix(camera)
        val lightIntensity = computeLightIntensity(frame)

        visualizePlanes(camera, projectionMatrix)

        drawPoints(projectionMatrix, viewMatrix, lightIntensity)

        val isSuccess =
            drawCenterPointer(camera, frame, projectionMatrix, viewMatrix, lightIntensity)

        mainThread {
            getBinding().putPoint.visibility = if (isSuccess) {
                VISIBLE
            } else {
                INVISIBLE
            }
        }

        if (isAddingPoint && pointerAttachment != null) {
            isAddingPoint = false
            pointAttachments.add(pointerAttachment!!)
            pointerAttachment = null
        }
//        if (pose == null && timer.isEmpty()) {
//            pose = camera.pose
//        }
    }

    private fun drawCenterPointer(
        camera: Camera,
        frame: Frame,
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray,
        lightIntensity: FloatArray
    ): Boolean {
        pointerAttachment?.anchor?.detach()
        val hitResults = frame.hitTest(centerX, centerY)

        val plane = hitResults.find { isValidPlane(camera, it) }
        return plane?.let {
            pointerAttachment = addSessionAnchorFromAttachment(pointerAttachment, it)
            drawObject(
                pointerObject,
                pointerAttachment,
                0.05F,
                projectionMatrix,
                viewMatrix,
                lightIntensity
            )

            true
        } ?: false
    }

    private fun drawPoints(
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray,
        lightIntensity: FloatArray
    ) {
        pointAttachments.forEach {
            drawObject(
                pointerObject,
                it,
                0.05F,
                projectionMatrix,
                viewMatrix,
                lightIntensity
            )
        }
    }

    override fun onTap(camera: Camera, frame: Frame, hitResults: List<HitResult>) {
        for (hit in hitResults) {
            val trackable = hit.trackable

            if (isValidPlane(camera, hit)) {

            }
        }
    }


    companion object {
        private const val TAG = "WallsActivity"

        private const val TIMER_VALUE_SECONDS = 3

        private var timer: TimeUnit = Seconds(TIMER_VALUE_SECONDS)
        private val TIMER_STEP = Millis(30)

        private val ANGLE_RANGE = Range(80F, 100F)
    }
}