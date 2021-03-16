package com.merseyside.ar.rendering

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.Matrix
import androidx.annotation.ColorInt
import com.google.ar.core.Anchor
import com.merseyside.ar.helpers.ArHelper
import com.merseyside.utils.ext.log
import com.merseyside.utils.ext.logMsg
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

class LineRenderer {
    private val vertices: MutableList<Vertex> = LinkedList()
    private var isDataChanged = false

    private var lineProgram: Int = 0

    private var mMVPMatrixHandle = 0

    private var vertexBuffer: FloatBuffer? = null
    var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private val vertexStride = FLOATS_PER_VERTEX * 4

    private val mModelMatrix = FloatArray(16)
    private val mModelViewMatrix = FloatArray(16)
    private val mModelViewProjectionMatrix = FloatArray(16)

    var mode = Mode.STROKE

    fun createOnGlThread(context: Context) {
        val vertexShader = ShaderUtil.loadGLShader(
            TAG,
            context,
            GL_VERTEX_SHADER,
            VERTEX_SHADER_NAME
        )

        val fragmentShader = ShaderUtil.loadGLShader(
            TAG,
            context,
            GL_FRAGMENT_SHADER,
            FRAGMENT_SHADER_NAME
        )

        lineProgram = glCreateProgram()
        glAttachShader(lineProgram, vertexShader)
        glAttachShader(lineProgram, fragmentShader)
        glLinkProgram(lineProgram)
        glUseProgram(lineProgram)

        glLineWidth(5F)

        Matrix.setIdentityM(mModelMatrix, 0)
    }

    fun addVertex(anchor: Anchor) {
        val _pose = ArHelper.getPose(anchor)
        val translation = FloatArray(4)
        _pose.getTranslation(translation, 0)
        vertices.add(translation.toVertex())

        isDataChanged = true
    }

    fun setColor(@ColorInt color: Int, alpha: Int) {
        this.color = floatArrayOf(
            Color.red(color).toFloat() / 255,
            Color.green(color).toFloat() / 255,
            Color.blue(color).toFloat() / 255,
            (alpha.toFloat() / 255).log()
        )
    }

    fun prepareVertices() {

        val verticesFloats = vertices.toFloatArray()

        vertexBuffer = ByteBuffer
            .allocateDirect(verticesFloats.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexBuffer?.put(verticesFloats)

        vertexBuffer?.position(0)

        isDataChanged = false
    }

    fun draw(cameraView: FloatArray?, cameraPerspective: FloatArray?) {
        ShaderUtil.checkGLError(TAG, "Before draw")

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)

        if (vertices.size > 1) {

            if (isDataChanged) prepareVertices()

            Matrix.multiplyMM(mModelViewMatrix, 0, cameraView, 0, mModelMatrix, 0)
            Matrix.multiplyMM(
                mModelViewProjectionMatrix,
                0,
                cameraPerspective,
                0,
                mModelViewMatrix,
                0
            )

            glUseProgram(lineProgram)
            ShaderUtil.checkGLError(TAG, "After glBindBuffer")
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            ShaderUtil.checkGLError(TAG, "After glBindBuffer")

            mPositionHandle = glGetAttribLocation(lineProgram, "a_Position")
            ShaderUtil.checkGLError(TAG, "After glGetAttribLocation")

            glEnableVertexAttribArray(mPositionHandle)
            ShaderUtil.checkGLError(TAG, "After glEnableVertexAttribArray")

            glVertexAttribPointer(
                mPositionHandle, FLOATS_PER_VERTEX,
                GL_FLOAT, false,
                vertexStride, vertexBuffer
            )
            ShaderUtil.checkGLError(TAG, "After glVertexAttribPointer")

            mColorHandle = glGetUniformLocation(lineProgram, "u_Color")

            glUniform4fv(mColorHandle, 1, color, 0)
            ShaderUtil.checkGLError(TAG, "After glUniform4fv")

            mMVPMatrixHandle = glGetUniformLocation(lineProgram, "u_ModelViewProjection")

            glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mModelViewProjectionMatrix, 0)
            ShaderUtil.checkGLError(TAG, "After glUniformMatrix4fv")

            val primitive =
                if (mode == Mode.STROKE) GL_LINE_LOOP
                else GL_TRIANGLE_FAN

            glDrawArrays(primitive, 0, vertices.size)
            ShaderUtil.checkGLError(TAG, "After glDrawArrays")

            glDisableVertexAttribArray(mPositionHandle)
            ShaderUtil.checkGLError(TAG, "After draw")
        }
    }

    fun reset() {
        vertices.clear()
        vertexBuffer?.clear()
        mode = Mode.STROKE
    }

    class Vertex(
        val x: Float,
        val y: Float,
        val z: Float,
    ) {
        override fun toString(): String {
            return "$x $y"
        }
    }

    fun List<Vertex>.toFloatArray(): FloatArray {
        return this@toFloatArray.flatMap {
            ArrayList<Float>().apply {
                add(it.x)
                add(it.y)
                add(it.z)
            }
        }.toFloatArray()
    }

    fun FloatArray.toVertex(): Vertex {
        return Vertex(get(0), get(1), get(2)).also {
            logMsg("toVertex ${it.x} ${it.y} ${it.z}")
        }
    }

    enum class Mode { SOLID, STROKE }


    companion object {
        private const val TAG = "LineRenderer"

        private const val FLOATS_PER_VERTEX = 3
        private const val BYTES_PER_FLOAT = Float.SIZE_BYTES
        private const val BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_VERTEX

        private const val VERTEX_SHADER_NAME = "shaders/line.vert"
        private const val FRAGMENT_SHADER_NAME = "shaders/line.frag"
    }
}