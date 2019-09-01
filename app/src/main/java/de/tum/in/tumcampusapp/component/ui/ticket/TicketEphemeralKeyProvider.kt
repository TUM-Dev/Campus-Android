package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.content.Context
import androidx.annotation.Size
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class TicketEphemeralKeyProvider(
    private val context: Context,
    private val onStringResponse: (String) -> Unit
) : EphemeralKeyProvider {

    override fun createEphemeralKey(
        @Size(min = 4) apiVersion: String,
        keyUpdateListener: EphemeralKeyUpdateListener
    ) {
        try {
            TUMCabeClient.getInstance(context).retrieveEphemeralKey(context, apiVersion,
                    object : Callback<HashMap<String, Any>> {
                        override fun onResponse(
                            call: Call<HashMap<String, Any>>,
                            response: Response<HashMap<String, Any>>
                        ) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                val id = responseBody.toString()
                                keyUpdateListener.onKeyUpdate(id)
                                onStringResponse(id)
                            }
                        }

                        override fun onFailure(call: Call<HashMap<String, Any>>, throwable: Throwable) {
                            Utils.log(throwable)
                        }
                    })
        } catch (e: NoPrivateKey) {
            Utils.log(e)
        }
    }
}
