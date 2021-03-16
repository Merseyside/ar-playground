package com.merseyside.ar.sample.main.view

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import com.google.ar.core.*
import com.merseyside.ar.R
import com.merseyside.ar.databinding.ActivityWallsBinding
import com.merseyside.ar.helpers.ArHelper
import com.merseyside.ar.rendering.LineRenderer
import com.merseyside.ar.rendering.ObjectRenderer
import com.merseyside.ar.rendering.PlaneAttachment
import com.merseyside.ar.rendering.SquareUtil
import com.merseyside.ar.sample.base.ArActivity
import com.merseyside.ar.sample.view.paletteView.PaletteHelper
import com.merseyside.archy.presentation.view.INVISIBLE
import com.merseyside.archy.presentation.view.VISIBLE
import com.merseyside.utils.ext.log
import com.merseyside.utils.ext.onClick
import com.merseyside.utils.mainThread
import com.merseyside.utils.time.Seconds
import com.merseyside.utils.time.TimeUnit
import kotlinx.coroutines.Job


class WallsActivity : ArActivity<ActivityWallsBinding>() {

    override fun performInjection(bundle: Bundle?) {}
    override fun getLayoutId() = R.layout.activity_walls
    override fun getToolbar() = null
    override fun getFragmentContainer() = null
    override fun getSurfaceViewId() = getBinding().surfaceView.id

    private var timerJob: Job? = null

    private var centerX = 0F
    private var centerY = 0F

    private var isShowingPlanes = true
    private var isShowingPoints = true

//    private var animator: BaseAnimator? = null

    private val pointerObject = ObjectRenderer()
    private val lineRenderer = LineRenderer()

    private var pointerAttachment: PlaneAttachment? = null

    private var isAddingPoint: Boolean = false
    private val pointAttachments: MutableList<PlaneAttachment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        centerX = (this.resources.displayMetrics.widthPixels / 2).toFloat()
        centerY = (this.resources.displayMetrics.heightPixels / 2).toFloat()

        getBinding().reset.onClick {
            reset()
        }

        getBinding().putPoint.onClick {
            isAddingPoint = true
        }

        getBinding().fillPolygon.onClick {
            lineRenderer.mode = LineRenderer.Mode.SOLID
            findAreaSquare()

            isShowingPlanes = false
            isShowingPoints = false
        }

        getBinding().palette.setPalette(PaletteHelper.getPaletteList()) {
            setColor(ContextCompat.getColor(this, it.resId), getBinding().opacity.value.toInt())
        }

        getBinding().opacity.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            setColor(ContextCompat.getColor(this@WallsActivity, getBinding().palette.getSelected().resId), value.toInt())
        })
    }

    private fun setColor(@ColorInt color: Int, alpha: Int) {
        lineRenderer.setColor(color, alpha)
    }

    private fun findAreaSquare() {
        var total = 0.0

        val size: Int = pointAttachments.size
        val arrayOfVertex = Array(size + 1) { FloatArray(2) }
        var pose0 = ArHelper.getPose(pointAttachments.first().anchor)
        arrayOfVertex[0][0] = pose0.tx()
        arrayOfVertex[0][1] = pose0.ty()
        for (i in 1 until pointAttachments.size) {
            val pose1 = ArHelper.getPose(pointAttachments[i].anchor)
            val distance = (ArHelper.getDistance(pose0, pose1) * 1000)/10.0f
            total += distance

            arrayOfVertex[i][0] = pose1.tx()
            arrayOfVertex[i][1] = pose1.ty()
            pose0 = pose1
        }

        Toast.makeText(this, "Distance: $total cm ${(SquareUtil.gaussFormulaToFindArea(arrayOfVertex))}", Toast.LENGTH_LONG).show()
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
        lineRenderer.reset()
        pointAttachments.forEach { it.anchor.detach() }
        pointAttachments.clear()

        isShowingPlanes = true
        isShowingPoints = true
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

        lineRenderer.createOnGlThread(this)

        pointerObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
    }

    override fun onDrawFrame(camera: Camera, frame: Frame) {
        val projectionMatrix = computeProjectionMatrix(camera)
        val viewMatrix = computeViewMatrix(camera)
        val lightIntensity = computeLightIntensity(frame)

        if (isShowingPlanes) visualizePlanes(camera, projectionMatrix)
        if (isShowingPoints) drawPoints(projectionMatrix, viewMatrix, lightIntensity)

        val isSuccess =
            drawCenterPointer(camera, frame, projectionMatrix, viewMatrix, lightIntensity)

        mainThread {
            getBinding().putPoint.visibility = if (isSuccess) {
                VISIBLE
            } else {
                INVISIBLE
            }

            getBinding().fillPolygon.visibility = if (pointAttachments.size > 2) {
                 VISIBLE
            } else {
                INVISIBLE
            }
        }

        if (isAddingPoint && pointerAttachment != null) {
            isAddingPoint = false
            pointAttachments.add(pointerAttachment!!)

            // Add vertex to lineRenderer
            lineRenderer.addVertex(pointerAttachment!!.anchor)

            pointerAttachment = null
        }

        drawLines(projectionMatrix, viewMatrix)
    }

    private fun drawLines(
        projectionMatrix: FloatArray,
        cameraMatrix: FloatArray
    ) {
        lineRenderer.draw(cameraMatrix, projectionMatrix)
    }

    private fun drawCenterPointer(
        camera: Camera,
        frame: Frame,
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray,
        lightIntensity: FloatArray
    ): Boolean {
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
        private const val TIMER_VALUE_SECONDS = 3

        private var timer: TimeUnit = Seconds(TIMER_VALUE_SECONDS)
    }
}