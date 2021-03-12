package com.merseyside.ar.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
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

    private lateinit var vertexBuffer: FloatBuffer
    var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private val vertexStride = FLOATS_PER_VERTEX * 4

    private val mModelMatrix = FloatArray(16)
    private val mModelViewMatrix = FloatArray(16)
    private val mModelViewProjectionMatrix = FloatArray(16)

    fun createOnGlThread(context: Context) {
        val vertexShader = ShaderUtil.loadGLShader(
            TAG,
            context,
            GLES20.GL_VERTEX_SHADER,
            VERTEX_SHADER_NAME
        )

        val fragmentShader = ShaderUtil.loadGLShader(
            TAG,
            context,
            GLES20.GL_FRAGMENT_SHADER,
            FRAGMENT_SHADER_NAME
        )

        lineProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(lineProgram, vertexShader)
        GLES20.glAttachShader(lineProgram, fragmentShader)
        GLES20.glLinkProgram(lineProgram)
        GLES20.glUseProgram(lineProgram)

        GLES20.glLineWidth(5F)

        Matrix.setIdentityM(mModelMatrix, 0)
    }

    fun addVertex(anchor: Anchor) {
        val _pose = getPose(anchor)
        val translation = FloatArray(4)
        _pose.getTranslation(translation, 0)
        vertices.add(translation.toVertex())

        isDataChanged = true
    }

    fun prepareVertices() {

        val verticesFloats = vertices.toFloatArray()

        vertexBuffer = ByteBuffer
            .allocateDirect(verticesFloats.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexBuffer.put(verticesFloats)

        vertexBuffer.position(0)

        isDataChanged = false
    }

    fun setColor(red: Float, green: Float, blue: Float, alpha: Float) {
        color[0] = red
        color[1] = green
        color[2] = blue
        color[3] = alpha
    }

    fun draw(cameraView: FloatArray?, cameraPerspective: FloatArray?) {
        ShaderUtil.checkGLError(TAG, "Before draw")

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

            GLES20.glUseProgram(lineProgram)
            ShaderUtil.checkGLError(TAG, "After glBindBuffer")
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            ShaderUtil.checkGLError(TAG, "After glBindBuffer")

            mPositionHandle = GLES20.glGetAttribLocation(lineProgram, "a_Position")
            ShaderUtil.checkGLError(TAG, "After glGetAttribLocation")

            GLES20.glEnableVertexAttribArray(mPositionHandle)
            ShaderUtil.checkGLError(TAG, "After glEnableVertexAttribArray")

            GLES20.glVertexAttribPointer(
                mPositionHandle, FLOATS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer
            )
            ShaderUtil.checkGLError(TAG, "After glVertexAttribPointer")

            mColorHandle = GLES20.glGetUniformLocation(lineProgram, "u_Color")

            GLES20.glUniform4fv(mColorHandle, 1, color, 0)
            ShaderUtil.checkGLError(TAG, "After glUniform4fv")

            mMVPMatrixHandle = GLES20.glGetUniformLocation(lineProgram, "u_ModelViewProjection")

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mModelViewProjectionMatrix, 0)
            ShaderUtil.checkGLError(TAG, "After glUniformMatrix4fv")

            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertices.size)
            ShaderUtil.checkGLError(TAG, "After glDrawArrays")

            GLES20.glDisableVertexAttribArray(mPositionHandle)
            ShaderUtil.checkGLError(TAG, "After draw")
        }
    }

    fun reset() {
        vertices.clear()
        vertexBuffer.clear()
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

    private val mPoseTranslation = FloatArray(3)
    private val mPoseRotation = FloatArray(4)

    private fun getPose(anchor: Anchor): Pose {
        val pose = anchor.pose
        pose.getTranslation(mPoseTranslation, 0)
        pose.getRotationQuaternion(mPoseRotation, 0)
        return Pose(mPoseTranslation, mPoseRotation)
    }

    companion object {
        private const val TAG = "LineRenderer"

        private const val FLOATS_PER_VERTEX = 3
        private const val BYTES_PER_FLOAT = Float.SIZE_BYTES
        private const val BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_VERTEX

        private const val VERTEX_SHADER_NAME = "shaders/line.vert"
        private const val FRAGMENT_SHADER_NAME = "shaders/line.frag"
    }
}