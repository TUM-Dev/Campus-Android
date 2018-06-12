package de.tum.`in`.tumcampusapp.api.app

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.exception.NoNetworkConnectionException
import de.tum.`in`.tumcampusapp.utils.NetUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!NetUtils.isConnected(context)) {
            throw NoNetworkConnectionException()
        }

        return chain.proceed(request)
    }

}