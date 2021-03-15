package com.merseyside.ar.rendering

import kotlin.math.abs

object SquareUtil {

    fun gaussFormulaToFindArea(arr: Array<FloatArray>): Double {
        val n = arr.size
        /** copy initial point to last row  */
        arr[n - 1][0] = arr[0][0]
        arr[n - 1][1] = arr[0][1]
        var det = 0.0
        /** add product of x coordinate of ith point with y coordinate of (i + 1)th point  */
        for (i in 0 until n - 1) det += (arr[i][0] * arr[i + 1][1]).toDouble()
        /** subtract product of y coordinate of ith point with x coordinate of (i + 1)th point  */
        for (i in 0 until n - 1) det -= (arr[i][1] * arr[i + 1][0]).toDouble()
        /** find absolute value and divide by 2  */
        det = abs(det)
        det /= 2.0
        return det
    }
}