package com.merseyside.ar.sample.utils

import android.app.Activity
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.exceptions.UnavailableException

fun isARCoreSupportedAndUpToDate(activity: Activity): Boolean {
    return when (ArCoreApk.getInstance().checkAvailability(activity)) {
        Availability.SUPPORTED_INSTALLED -> true
        Availability.SUPPORTED_APK_TOO_OLD, Availability.SUPPORTED_NOT_INSTALLED -> {
            try {
                // Request ARCore installation or update if needed.
                when (ArCoreApk.getInstance().requestInstall(activity, true)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        Log.i("AR", "ARCore installation requested.")
                        false
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> true
                }
            } catch (e: UnavailableException) {
                Log.e("AR", "ARCore not installed", e)
                false
            }
        }

        Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->
            // This device is not supported for AR.
            false

        Availability.UNKNOWN_CHECKING -> {
            // ARCore is checking the availability with a remote query.
            // This function should be called again after waiting 200 ms to determine the query result.
            false
        }
        Availability.UNKNOWN_ERROR, Availability.UNKNOWN_TIMED_OUT -> {
            // There was an error checking for AR availability. This may be due to the device being offline.
            // Handle the error appropriately.
            false
        }
    }
}