package com.merseyside.ar.helpers

import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import kotlin.math.sqrt

object ArHelper {

    private val mPoseTranslation = FloatArray(3)
    private val mPoseRotation = FloatArray(4)


    fun getPose(anchor: Anchor): Pose {
        val pose = anchor.pose
        pose.getTranslation(mPoseTranslation, 0)
        pose.getRotationQuaternion(mPoseRotation, 0)
        return Pose(mPoseTranslation, mPoseRotation)
    }

    fun getDistance(pose0: Pose, pose1: Pose): Double {
        val dx = pose0.tx() - pose1.tx()
        val dy = pose0.ty() - pose1.ty()
        val dz = pose0.tz() - pose1.tz()
        return sqrt((dx * dx + dz * dz + dy * dy).toDouble())
    }
}