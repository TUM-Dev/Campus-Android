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
import de.tum.`in`.tumcampusapp.utils.Const
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class CafeteriaAPIClient(private val apiService: CafeteriaAPIService) {

    fun getMenus(cacheControl: CacheControl, cafeteriaLocation: CafeteriaLocation): Call<CafeteriaResponse> {
        val defaultCalendarInstance = Calendar.getInstance(TimeZone.getDefault())
        val year: Int = defaultCalendarInstance.get(Calendar.YEAR)
        val calendarWeek = getCalendarWeek(year, defaultCalendarInstance)

        return if(calendarWeek in 1..9){
            apiService.getMenus(cacheControl.header, cafeteriaLocation.toSlug(), year, "0$calendarWeek")
        } else {
            apiService.getMenus(cacheControl.header, cafeteriaLocation.toSlug(), year, calendarWeek.toString())
        }
    }

    private fun getCalendarWeek(year: Int, defaultCalendarInstance: Calendar): Int {
        val cafeteriaReopeningCalendarInstance = setCalendarToFirstFullWeekAfterHolidays(year)
        val isFirstWeekOfYearPartial = doesYearStartOnPartialWeek(year)

        // Since the cafeteria only reopens for the first full week of uni in the new year, no menus are provided for weeks before 7.1 by the eat-api
        //      => If the current calendarWeek is less than the calendarWeek of the first full week after 7.1 (end of holidays), set it to the first full week after the holidays,
        //      to have some menu to display, that makes sense semantically
        return if (defaultCalendarInstance.get(Calendar.WEEK_OF_YEAR) < cafeteriaReopeningCalendarInstance.get(Calendar.WEEK_OF_YEAR))
            if (isFirstWeekOfYearPartial) cafeteriaReopeningCalendarInstance.get(Calendar.WEEK_OF_YEAR) - 1 else cafeteriaReopeningCalendarInstance.get(Calendar.WEEK_OF_YEAR)
        else
            if (isFirstWeekOfYearPartial) defaultCalendarInstance.get(Calendar.WEEK_OF_YEAR) - 1 else defaultCalendarInstance.get(Calendar.WEEK_OF_YEAR)
    }

    private fun doesYearStartOnPartialWeek(year: Int): Boolean {
        val yearStartCalendarInstance = Calendar.getInstance(TimeZone.getDefault())
        yearStartCalendarInstance.set(year, 0, 1)

        // Check if offset is needed to adjust for the year starting in the middle of a week (ie.: not on monday)
        return yearStartCalendarInstance.get(Calendar.DAY_OF_WEEK) != Const.MONDAY
    }

    private fun setCalendarToFirstFullWeekAfterHolidays(year: Int): Calendar {
        val cafeteriaReopeningCalendarInstance = Calendar.getInstance(TimeZone.getDefault())

        // The winter holidays end on 7.1 (after Epiphany on 6.1)
        cafeteriaReopeningCalendarInstance.set(year, 0, 7)

        when (cafeteriaReopeningCalendarInstance.get(Calendar.DAY_OF_WEEK)) {
            Calendar.TUESDAY -> cafeteriaReopeningCalendarInstance.add(Calendar.DAY_OF_MONTH, 6)
            Calendar.WEDNESDAY -> cafeteriaReopeningCalendarInstance.add(Calendar.DAY_OF_MONTH, 5)
            Calendar.THURSDAY -> cafeteriaReopeningCalendarInstance.add(Calendar.DAY_OF_MONTH, 4)
            Calendar.FRIDAY -> cafeteriaReopeningCalendarInstance.add(Calendar.DAY_OF_MONTH, 3)
            Calendar.SATURDAY -> cafeteriaReopeningCalendarInstance.add(Calendar.DAY_OF_MONTH, 2)
            Calendar.SUNDAY -> cafeteriaReopeningCalendarInstance.add(Calendar.DAY_OF_MONTH, 1)
        }

        return cafeteriaReopeningCalendarInstance
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