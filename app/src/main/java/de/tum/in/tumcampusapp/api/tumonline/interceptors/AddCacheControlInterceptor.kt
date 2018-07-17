package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

class AddCacheControlInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Check whether this request is a cacheable request
        val forceRefresh = request.header("x-force-refresh") == "true"
        val modifiedRequestBuilder = if (forceRefresh) {
            request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK)
        } else {
            request.newBuilder()
        }

        return chain.proceed(modifiedRequestBuilder.build())
    }

}