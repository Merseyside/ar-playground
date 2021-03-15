package com.merseyside.ar.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.google.ar.core.Anchor
import com.merseyside.ar.helpers.ArHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


class PointRenderer(private val mColor: FloatArray) {
    private var program: Int = 0
    private val modelMatrix = FloatArray(16)

    var centerX = 0f
    var centerY = 0f
    var centerZ = 0f

    var radius = 0f
    private val mVertexBuffer: FloatBuffer
    private val mDrawListBuffer: ShortBuffer
    private val mDrawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

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

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        GLES20.glUseProgram(program)
    }

    fun setCoords(anchor: Anchor) {
        val _pose = ArHelper.getPose(anchor)
        val translation = FloatArray(4)
        _pose.getTranslation(translation, 0)

        centerX = translation[0]
        centerY = translation[1]
        centerZ = translation[2]
    }

    fun draw(cameraView: FloatBuffer, perspectiveView: FloatBuffer) {



        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glUseProgram(program)
        val mPositionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        val vertexStride = COORDS_PER_VERTEX * 4
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            mVertexBuffer
        )
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(program, "aColor"), 1, mColor, 0)
        GLES20.glUniform2f(
            GLES20.glGetUniformLocation(program, "aCirclePosition"),
            centerX,
            centerY
        )
        GLES20.glUniform1f(
            GLES20.glGetUniformLocation(program, "aRadius"),
            radius
        )
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mDrawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            mDrawListBuffer
        )
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {

        private const val TAG = "PointRenderer"

        private const val VERTEX_SHADER_NAME = "shaders/point.vert"
        private const val FRAGMENT_SHADER_NAME = "shaders/point.vert"

        private const val COORDS_PER_VERTEX = 2
        private val VERTEX_COORDINATES = floatArrayOf(
            -1f, 1f,  // top left
            -1f, -1f,  // bottom left
            1f, -1f,  // bottom right
            1f, 1f
        )
    }

    init {

        val vertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
        vertexByteBuffer.order(ByteOrder.nativeOrder())
        mVertexBuffer = vertexByteBuffer.asFloatBuffer()
        mVertexBuffer.put(VERTEX_COORDINATES)
        mVertexBuffer.position(0)
        val drawByteBuffer = ByteBuffer.allocateDirect(mDrawOrder.size * 2)
        drawByteBuffer.order(ByteOrder.nativeOrder())
        mDrawListBuffer = drawByteBuffer.asShortBuffer()
        mDrawListBuffer.put(mDrawOrder)
        mDrawListBuffer.position(0)

        Matrix.setIdentityM(modelMatrix, 0)
    }
}