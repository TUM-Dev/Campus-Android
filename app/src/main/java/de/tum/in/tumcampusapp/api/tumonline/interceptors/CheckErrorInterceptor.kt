package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import android.content.Context
import com.tickaroo.tikxml.TikXml
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InvalidTokenException
import de.tum.`in`.tumcampusapp.api.tumonline.model.Error
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.InterruptedIOException

class CheckErrorInterceptor(private val context: Context) : Interceptor {

    private val tikXml = TikXml.Builder()
            .exceptionOnUnreadXml(false)
            .build()

    @Throws(InterruptedIOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Send the request to TUMonline
        val response = chain.proceed(request)
        val peekBody = response.peekBody(Long.MAX_VALUE)

        // The server always returns 200. To detect errors, we attempt to parse the response into
        // an Error. If this fails, we know that we got a non-error response from TUMonline.
        val error = tryOrNull { tikXml.read(peekBody.source(), Error::class.java) }
        error?.let {
            throw it.exception.also {
                if (it is InvalidTokenException) {
                    // If it is an InvalidTokenException, we disable interaction with TUMonline.
                    Utils.setSetting(context, Const.TUMO_DISABLED, true)
                }
            }
        }

        // Because the request did not return an Error, we can re-enable TUMonline request
        // if they have been disabled before
        Utils.setSetting(context, Const.TUMO_DISABLED, false)

        return response
    }
}