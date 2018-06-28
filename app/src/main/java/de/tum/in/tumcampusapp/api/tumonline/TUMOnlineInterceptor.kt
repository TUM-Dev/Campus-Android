package de.tum.`in`.tumcampusapp.api.tumonline

import android.content.Context
import android.preference.PreferenceManager
import com.tickaroo.tikxml.TikXml
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InactiveTokenException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InvalidTokenException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.MissingPermissionException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.UnknownErrorException
import de.tum.`in`.tumcampusapp.api.tumonline.model.Error
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import okhttp3.Interceptor
import okhttp3.Response

class TUMOnlineInterceptor(private val context: Context) : Interceptor {

    /**
     * Token is active but permission for this feature was not given
     */
    private val NO_FUNCTION_RIGHTS = "Keine Rechte für Funktion"

    /**
     * Token not valid or not activated by user
     */
    private val TOKEN_NOT_CONFIRMED = "Token ist nicht bestätigt oder ungültig!"

    private var accessToken: String? = loadAccessTokenFromPreferences(context)

    @Throws(InactiveTokenException::class,
            InvalidTokenException::class,
            MissingPermissionException::class,
            UnknownErrorException::class
    )
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // check for special requests
        val segments = request.url().pathSegments()
        val isTokenRequest = segments.contains("requestToken")
        val isTokenConfirmationCheck = segments.contains("isTokenConfirmed")

        // If there were some requests that failed and we verified that the token is not
        // active anymore, block all requests directly
        if (!isTokenRequest ||
                (!isTokenConfirmationCheck
                    && Utils.getSettingBool(context, Const.TUMO_DISABLED, false))) {
                throw InactiveTokenException()
        }

        // Add the access token as a parameter to the URL
        var modifiedUrl = request.url()
        accessToken?.let {
            modifiedUrl = request.url()
                    .newBuilder()
                    .addQueryParameter("pToken", it)
                    .build()
        }
        val modifiedRequest = request
                .newBuilder()
                .url(modifiedUrl)
                .build()

        // send the request to TUMonline
        val response = chain.proceed(modifiedRequest)

        val tikXml = TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()

        val error = tryOrNull {
            tikXml.read(response.body()?.source(), Error::class.java)
        }

        if (error == null){
            // valid response
            return response
        }

        when (error.message) {
            NO_FUNCTION_RIGHTS -> throw MissingPermissionException()
            TOKEN_NOT_CONFIRMED -> {
                Utils.setSetting(context, Const.TUMO_DISABLED, true)
                throw InactiveTokenException()
            }
            else -> throw UnknownErrorException()
        }
    }

    private fun loadAccessTokenFromPreferences(context: Context): String? {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(Const.ACCESS_TOKEN, null)
    }

}