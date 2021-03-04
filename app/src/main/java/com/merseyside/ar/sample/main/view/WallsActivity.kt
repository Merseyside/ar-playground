package com.merseyside.ar.sample.main.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.*
import com.merseyside.ar.R
import com.merseyside.ar.databinding.ActivityWallsBinding
import com.merseyside.ar.rendering.PlaneRenderer
import com.merseyside.ar.sample.base.ArActivity
import com.merseyside.utils.ext.delay
import com.merseyside.utils.ext.log
import com.merseyside.utils.time.Millis
import com.merseyside.utils.time.Seconds
import com.merseyside.utils.time.TimeUnit
import com.merseyside.utils.time.minus
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

class WallsActivity : ArActivity<ActivityWallsBinding>() {
    override fun performInjection(bundle: Bundle?) {}
    override fun getLayoutId() = R.layout.activity_walls
    override fun getToolbar() = null
    override fun getFragmentContainer() = null
    override fun getSurfaceViewId() = getBinding().surfaceView.id

    private var timerJob: Job? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var accelSensor: Sensor

    private var angle: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private var listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            event.values[0].log()
            when (event.sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    angle = event.values[1]
                }
            }

            if (timerJob == null || abs(angle) < 5) {
                timerJob = lifecycleScope.launch {
                    delay(TIMER_STEP)

                    timer -= TIMER_STEP
                }
            }
        }
    }

    override fun onSurfaceCreated() {
        // TODO - set up the objects
        // 1
//            vikingObject.createOnGlThread(this@MainActivity, getString(R.string.model_viking_obj), getString(
//                R.string.model_viking_png))


        // 2
        //targetObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
    }

    override fun onDrawFrame(camera: Camera, frame: Frame) {
        val projectionMatrix = computeProjectionMatrix(camera)
        val viewMatrix = computeViewMatrix(camera)
        val lightIntensity = computeLightIntensity(frame)

        visualizePlanes(camera, projectionMatrix)
    }

    override fun onTap(camera: Camera, frame: Frame, hitResults: List<HitResult>) {
        for (hit in hitResults) {
            val trackable = hit.trackable

            if ((trackable is Plane
                        && trackable.isPoseInPolygon(hit.hitPose)
                        && PlaneRenderer.calculateDistanceToPlane(hit.hitPose, camera.pose) > 0)
            ) {

            }
        }
    }

    private var timer: TimeUnit = Seconds(5)

    companion object {
        private val TIMER_STEP = Millis(50)
    }
}