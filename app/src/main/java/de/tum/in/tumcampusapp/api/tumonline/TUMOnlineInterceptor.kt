package de.tum.`in`.tumcampusapp.api.tumonline

import android.content.Context
import android.preference.PreferenceManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoNetworkConnectionException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InactiveTokenException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InvalidTokenException
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import okhttp3.Interceptor
import okhttp3.Response

class TUMOnlineInterceptor(private val context: Context) : Interceptor {

    private var accessToken: String? = loadAccessTokenFromPreferences(context)

    private val isTokenInactive: Boolean
        get() {
            val tokenConfirmation = TUMOnlineClient.getInstance(context)
                    .getTokenConfirmation()
                    .execute()
                    .body() ?: return false

            return tokenConfirmation.isConfirmed.also { value ->
                Utils.setSetting(context, Const.TUMO_DISABLED, value)
            }
        }

    @Throws(NoNetworkConnectionException::class,
            InactiveTokenException::class, InvalidTokenException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetUtils.isConnected(context)) {
            throw NoNetworkConnectionException()
        }

        val request = chain.request()

        val segments = request.url().pathSegments()
        val isTokenRequest = segments.contains("requestToken")
        val isTokenConfirmationCheck = segments.contains("isTokenConfirmed")

        if (!isTokenRequest) {
            // If there were some requests that failed and we verified that the token is not
            // active anymore, block all requests directly
            if (!isTokenConfirmationCheck
                    && Utils.getSettingBool(context, Const.TUMO_DISABLED, false)) {
                throw InactiveTokenException()
            }

            // TODO: See TUMOnlineRequest
        }

        // Add the access token as a parameter to the URL
        val modifiedUrl = request
                .url()
                .newBuilder()
                .addQueryParameter("pToken", accessToken)
                .build()

        val modifiedRequest = request
                .newBuilder()
                .url(modifiedUrl)
                .build()

        val response = chain.proceed(modifiedRequest)

        // TODO: Check if the request returns an error
        // If so, request a new token and repeat the request

        return response
    }

    private fun loadAccessTokenFromPreferences(context: Context): String? {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(Const.ACCESS_TOKEN, null)
    }

}