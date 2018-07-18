package de.tum.`in`.tumcampusapp.api.tumonline

import android.content.Context
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import de.tum.`in`.tumcampusapp.api.app.Helper
import de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.TUMOnlineInterceptor
import de.tum.`in`.tumcampusapp.api.tumonline.model.AccessToken
import de.tum.`in`.tumcampusapp.api.tumonline.model.TokenConfirmation
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CreateEventResponse
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.DeleteEventResponse
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.Events
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsResponse
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureDetailsResponse
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Employee
import de.tum.`in`.tumcampusapp.component.tumui.person.model.IdentitySet
import de.tum.`in`.tumcampusapp.component.tumui.person.model.PersonList
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.TuitionList
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import okhttp3.Cache
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Retrofit

class TUMOnlineClient(private val apiService: TUMOnlineAPIService) {

    fun getCalendar(force: Boolean = false): Call<Events> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getCalendar(
                Const.CALENDAR_MONTHS_BEFORE, Const.CALENDAR_MONTHS_AFTER, cacheControl)
    }

    fun createEvent(calendarItem: CalendarItem, eventId: String?): Call<CreateEventResponse> {
        val start = DateTimeUtils.getDateTimeString(calendarItem.eventStart)
        val end = DateTimeUtils.getDateTimeString(calendarItem.eventEnd)
        return apiService.createCalendarEvent(
                calendarItem.title, calendarItem.description, start, end, eventId)
    }

    fun deleteEvent(eventId: String): Call<DeleteEventResponse> {
        return apiService.deleteCalendarEvent(eventId)
    }

    fun getTuitionFeesStatus(force: Boolean = false): Call<TuitionList> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getTuitionFeesStatus(cacheControl)
    }

    fun getPersonalLectures(force: Boolean = false): Call<LecturesResponse> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getPersonalLectures(cacheControl)
    }

    fun getLectureDetails(id: String, force: Boolean = false): Call<LectureDetailsResponse> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getLectureDetails(id, cacheControl)
    }

    fun getLectureAppointments(id: String, force: Boolean = false): Call<LectureAppointmentsResponse> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getLectureAppointments(id, cacheControl)
    }

    fun searchLectures(query: String): Call<LecturesResponse> {
        return apiService.searchLectures(query)
    }

    fun getPersonDetails(id: String, force: Boolean = false): Call<Employee> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getPersonDetails(id, cacheControl)
    }

    fun searchPerson(query: String): Call<PersonList> {
        return apiService.searchPerson(query)
    }

    fun getGrades(force: Boolean = false): Call<ExamList> {
        val cacheControl = if (force) NO_CACHE else ONLY_IF_CACHED
        return apiService.getGrades(cacheControl)
    }

    fun getTokenConfirmation(): Call<TokenConfirmation> = apiService.getTokenConfirmation()

    fun requestToken(username: String, tokenName: String): Call<AccessToken> {
        return apiService.requestToken(username, tokenName)
    }

    fun getIdentity(): Call<IdentitySet> = apiService.getIdentity()

    fun uploadSecret(token: String, secret: String): Call<TokenConfirmation> {
        return apiService.uploadSecret(token, secret)
    }

    companion object {

        private const val BASE_URL = "https://campus.tum.de/tumonline/"
        private const val CACHE_SIZE: Long = 10 * 1024 * 1024; // 10 MB

        private const val NO_CACHE = "no-cache"
        private const val ONLY_IF_CACHED = "only-if-cached"

        private var client: TUMOnlineClient? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): TUMOnlineClient {
            if (client == null) {
                client = buildAPIClient(context)
            }

            return client!!
        }

        private fun buildAPIClient(context: Context): TUMOnlineClient {
            val cache = Cache(context.cacheDir, CACHE_SIZE)

            val client = Helper.getOkHttpClient(context)
                    .newBuilder()
                    .cache(cache)
                    //.addInterceptor(AddCacheControlInterceptor())
                    .addNetworkInterceptor(TUMOnlineInterceptor(context))
                    .build()

            // TODO: Add TypeConverter for date strings
            val tikXml = TikXml.Builder()
                    .addTypeConverter(DateTime::class.java, DateTimeConverter())
                    .exceptionOnUnreadXml(false)
                    .build()
            val xmlConverterFactory = TikXmlConverterFactory.create(tikXml)

            val apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(xmlConverterFactory)
                    .build()
                    .create(TUMOnlineAPIService::class.java)
            return TUMOnlineClient(apiService)
        }

    }

}