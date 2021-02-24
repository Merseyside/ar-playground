package com.merseyside.core.utils

import android.content.Context
import com.merseyside.utils.exception.NoInternetConnection
import com.merseyside.utils.network.isOnline
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptor(private val context: Context) : Interceptor {

    @Throws(NoInternetConnection::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (isOnline(context)) {
            return chain.proceed(chain.request())
        } else {
            throw NoInternetConnection()
        }
    }
}