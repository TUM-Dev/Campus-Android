package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class CacheResponseInterceptor : Interceptor {

    private val cachingHelper = CachingHelper()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val url = response.request.url.toString()
        val isCacheable = cachingHelper.isCacheable(url)

        return if (response.isSuccessful && isCacheable) {
            cachingHelper.updateCacheControlHeader(url, response)
        } else {
            response
        }
    }
}