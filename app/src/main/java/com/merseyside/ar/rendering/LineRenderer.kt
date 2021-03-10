package com.merseyside.ar.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.merseyside.ar.helpers.WorldToScreenHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*


class LineRenderer {

    private var vertices: MutableList<Vertex> = LinkedList()
    private lateinit var vertexData: FloatBuffer

    private var lineProgram = 0
    private var linePositionParam = 0
    private var lineColorParam = 0
    private var lineMatrixParam = 0

    private var isDataChanged = false

    private val matrix = FloatArray(16)

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

        linePositionParam = GLES20.glGetAttribLocation(lineProgram, "a_Position")
        lineColorParam = GLES20.glGetAttribLocation(lineProgram, "u_Color")
        lineMatrixParam = GLES20.glGetAttribLocation(lineProgram, "u_Matrix")

        ShaderUtil.checkGLError(TAG, "Program parameters")

        bindData()
    }

    private fun bindData() {
        lineColorParam = GLES20.glGetUniformLocation(lineProgram, "u_Color")
        GLES20.glUniform4f(lineColorParam, 1.0F, 1.0F, 1.0F, 1.0F)
        linePositionParam = GLES20.glGetUniformLocation(lineProgram, "a_Position")
        vertexData.position(0)
        GLES20.glVertexAttribPointer(linePositionParam, 3, GLES20.GL_FLOAT, false, 0, vertexData)
        GLES20.glEnableVertexAttribArray(linePositionParam)
    }

    private fun bindMatrix(perspectiveMatrix: FloatArray, viewMatrix: FloatArray) {
        Matrix.multiplyMM(matrix, 0, perspectiveMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(lineMatrixParam, 1, false, matrix, 0)
    }

    fun addVertex(
        screenWidth: Int,
        screenHeight: Int,
        modelMatrix: FloatArray,
        cameraMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        val world2screenMatrix: FloatArray =
            WorldToScreenHelper.calculateWorld2CameraMatrix(modelMatrix, cameraMatrix, projectionMatrix)
        val anchor_2d: DoubleArray = WorldToScreenHelper.world2Screen(screenWidth, screenHeight, world2screenMatrix)

        vertices.add(anchor_2d.toVertex())
        isDataChanged = true
    }

    fun prepareData() {
        val verticesFloats = vertices.toFloatArray()

        vertexData = ByteBuffer
            .allocateDirect(verticesFloats.size * Float.SIZE_BITS)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexData.put(verticesFloats)

        isDataChanged = false
    }

    fun draw(
        cameraView: FloatArray,
        cameraPerspective: FloatArray,
    ) {
        if (vertices.size > 1 && isDataChanged) prepareData()

        bindMatrix(cameraPerspective, cameraView)

        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertices.size)
    }

    class Vertex(
        val x: Float,
        val y: Float,
        val z: Float
    )

    fun List<Vertex>.toFloatArray(): FloatArray {
        return this@toFloatArray.flatMap {
            ArrayList<Float>().apply {
                add(it.x)
                add(it.y)
                add(it.z)
            }
        }.toFloatArray()
    }

    fun DoubleArray.toVertex(): Vertex {
        return Vertex(get(0).toFloat(), get(1).toFloat(), get(2).toFloat())
    }

    companion object {
        private const val TAG = "LineRenderer"
        
        private const val VERTEX_SHADER_NAME = "line.vert"
        private const val FRAGMENT_SHADER_NAME = "line.frag"
    }
}