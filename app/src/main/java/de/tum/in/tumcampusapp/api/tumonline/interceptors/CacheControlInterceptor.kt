package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheControlInterceptor : Interceptor {

    private val cachingHelper = CachingHelper()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url().toString()
        val cacheControlHeader = request.header("Cache-Control")

        val modifiedRequestBuilder = if (cacheControlHeader != "no-cache") {
            val cacheControl = getCacheControl(url)
            request.newBuilder()
                    .removeHeader("Cache-Control")
                    .addHeader("Cache-Control", cacheControl.toString())
        } else {
            request.newBuilder()
        }

        val modifiedRequest = modifiedRequestBuilder
                //.removeHeader("Connection")
                //.addHeader("Connection","close")
                .build()

        return chain.proceed(modifiedRequest)
    }

    private fun getCacheControl(url: String): CacheControl {
        val duration = cachingHelper.getCachingDuration(url)
        return CacheControl.Builder()
                .maxAge(duration.standardDays.toInt(), TimeUnit.DAYS)
                .build()
    }

}