package de.tum.`in`.tumcampusapp.api.cafeteria

import android.content.Context
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.app.DateSerializer
import de.tum.`in`.tumcampusapp.api.app.Helper
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.CacheResponseInterceptor
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaResponse
import de.tum.`in`.tumcampusapp.utils.CacheManager
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CafeteriaAPIClient(private val apiService: CafeteriaAPIService) {

    fun getMenus(cacheControl: CacheControl): Call<CafeteriaResponse> {
        return apiService.getMenus(cacheControl.header)
    }

    companion object {

        private const val BASE_URL = "https://www.devapp.it.tum.de/mensaapp/"

        private var apiClient: CafeteriaAPIClient? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): CafeteriaAPIClient {
            if (apiClient == null) {
                apiClient = buildAPIClient(context)
            }

            return apiClient!!
        }

        private fun buildAPIClient(context: Context): CafeteriaAPIClient {
            // We cache the cafeteria menu for one day. We use Interceptors to add the appropriate
            // cache-control headers to the response.
            val cacheManager = CacheManager(context)

            val client = Helper.getOkHttpClient(context)
                    .newBuilder()
                    .cache(cacheManager.cache)
                    .addNetworkInterceptor(CacheResponseInterceptor())
                    .build()

            val gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, DateSerializer())
                    .create()

            val apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(CafeteriaAPIService::class.java)

            return CafeteriaAPIClient(apiService)
        }

    }

}