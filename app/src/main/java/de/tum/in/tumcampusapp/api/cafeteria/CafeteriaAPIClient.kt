package de.tum.`in`.tumcampusapp.api.cafeteria

import android.content.Context
import com.google.gson.GsonBuilder
import de.tum.`in`.tumcampusapp.api.app.ApiHelper
import de.tum.`in`.tumcampusapp.api.app.DateSerializer
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.CacheResponseInterceptor
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaLocation
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.CafeteriaResponse
import de.tum.`in`.tumcampusapp.utils.CacheManager
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class CafeteriaAPIClient(private val apiService: CafeteriaAPIService) {

    fun getMenus(cacheControl: CacheControl, cafeteriaLocation: CafeteriaLocation): Call<CafeteriaResponse> {
        val defaultCalendarInstance = Calendar.getInstance(TimeZone.getDefault())
        val year: Int = defaultCalendarInstance.get(Calendar.YEAR)
        val calendarWeek: Int = defaultCalendarInstance.get(Calendar.WEEK_OF_YEAR)

        return if(calendarWeek in 1..9){
            apiService.getMenus(cacheControl.header, cafeteriaLocation, year, "0$calendarWeek")
        } else {
            apiService.getMenus(cacheControl.header, cafeteriaLocation, year, calendarWeek.toString())
        }
    }

    companion object {

        private const val BASE_URL = "https://tum-dev.github.io/eat-api/"

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

            val client = ApiHelper.getOkHttpClient(context)
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