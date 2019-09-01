package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import android.content.Context
import android.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.utils.Const
import okhttp3.Interceptor
import okhttp3.Response

class AddTokenInterceptor(private val context: Context) : Interceptor {

    private val accessToken: String?
        get() = loadAccessTokenFromPreferences(context)

    private fun loadAccessTokenFromPreferences(context: Context): String? {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(Const.ACCESS_TOKEN, null)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var url = request.url()

        // Do not add any token for requesting a new token. This would result in a 404
        if (url.encodedPath().contains("requestToken")) {
            return chain.proceed(request)
        }

        accessToken?.let {
            url = url
                    .newBuilder()
                    .addQueryParameter("pToken", it)
                    .build()
        }

        val modifiedRequest = request.newBuilder().url(url).build()
        return chain.proceed(modifiedRequest)
    }
}