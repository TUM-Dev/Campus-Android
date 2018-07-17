package de.tum.`in`.tumcampusapp.api.tumonline

import android.content.Context
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import de.tum.`in`.tumcampusapp.api.app.Helper
import de.tum.`in`.tumcampusapp.api.tumonline.interceptors.AddCacheControlInterceptor
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
import retrofit2.Call
import retrofit2.Retrofit

class TUMOnlineClient(private val apiService: TUMOnlineAPIService) {

    /*
    fun getCalendarRequest(monthsBefore: Int, monthsAfter: Int): TUMOnlineRequest<Events> {
        val call = apiService.getCalendar(monthsBefore, monthsAfter)
        return TUMOnlineRequest(call, Events::class.java, CachingDuration.ONE_DAY)
    }

    fun getCreateEventRequest(calendarItem: CalendarItem,
                              eventId: String?): TUMOnlineRequest<CreateEventResponse> {
        val start = DateTimeUtils.getDateTimeString(calendarItem.eventStart)
        val end = DateTimeUtils.getDateTimeString(calendarItem.eventEnd)
        val call = apiService.createCalendarEvent(calendarItem.title, calendarItem.description, start, end, eventId)
        return TUMOnlineRequest(call, CreateEventResponse::class.java, CachingDuration.NONE)
    }

    fun getDeleteEventRequest(eventId: String): TUMOnlineRequest<DeleteEventResponse> {
        val call = apiService.deleteCalendarEvent(eventId)
        return TUMOnlineRequest(call, DeleteEventResponse::class.java, CachingDuration.NONE);
    }

    fun getTuitionFeesRequest(): TUMOnlineRequest<TuitionList> {
        val call = apiService.getTuitionFeesStatus()
        return TUMOnlineRequest(call, TuitionList::class.java, CachingDuration.ONE_DAY)
    }

    fun getPersonalLecturesRequest(): TUMOnlineRequest<LecturesResponse> {
        val call = apiService.getPersonalLectures()
        return TUMOnlineRequest(call, LecturesResponse::class.java, CachingDuration.ONE_DAY)
    }

    fun getLectureDetailsRequest(id: String): TUMOnlineRequest<LectureDetailsResponse> {
        val call = apiService.getLectureDetails(id)
        return TUMOnlineRequest(call, LectureDetailsResponse::class.java, CachingDuration.FIVE_DAY)
    }

    fun getLectureAppointmentsRequest(id: String): TUMOnlineRequest<LectureAppointmentsResponse> {
        val call = apiService.getLectureAppointments(id)
        return TUMOnlineRequest(call, LectureAppointmentsResponse::class.java, CachingDuration.FIVE_DAY)
    }

    fun getSearchLecturesRequest(query: String): TUMOnlineRequest<LecturesResponse> {
        val call = apiService.searchLectures(query)
        return TUMOnlineRequest(call, LecturesResponse::class.java, CachingDuration.NONE)
    }

    fun getPersonDetailsRequest(id: String): TUMOnlineRequest<Employee> {
        val call = apiService.getPersonDetails(id)
        return TUMOnlineRequest(call, Employee::class.java, CachingDuration.FIVE_DAY)
    }

    fun getSearchPersonRequest(query: String): TUMOnlineRequest<PersonList> {
        val call = apiService.searchPerson(query)
        return TUMOnlineRequest(call, PersonList::class.java, CachingDuration.NONE)
    }

    fun getGradesRequest(): TUMOnlineRequest<ExamList> {
        val call = apiService.getGrades()
        return TUMOnlineRequest(call, ExamList::class.java, CachingDuration.ONE_DAY)
    }

    fun getTokenConfirmationRequest(): TUMOnlineRequest<TokenConfirmation> {
        val call = apiService.getTokenConfirmation()
        return TUMOnlineRequest(call, TokenConfirmation::class.java, CachingDuration.NONE)
    }

    fun getRequestTokenRequest(username: String, tokenName: String): TUMOnlineRequest<AccessToken> {
        val call = apiService.requestToken(username, tokenName)
        return TUMOnlineRequest(call, AccessToken::class.java, CachingDuration.NONE)
    }

    fun getIdentityRequest(): TUMOnlineRequest<IdentitySet> {
        val call = apiService.getIdentity()
        return TUMOnlineRequest(call, IdentitySet::class.java, CachingDuration.NONE)
    }

    fun getUploadSecretRequest(token: String, secret: String): TUMOnlineRequest<TokenConfirmation> {
        val call = apiService.uploadSecret(token, secret)
        return TUMOnlineRequest(call, TokenConfirmation::class.java, CachingDuration.NONE)
    }
    */

    fun getCalendar(force: Boolean = false): Call<Events> {
        return apiService.getCalendar(
                Const.CALENDAR_MONTHS_BEFORE, Const.CALENDAR_MONTHS_AFTER, force)
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
        return apiService.getTuitionFeesStatus(force)
    }

    fun getPersonalLectures(force: Boolean = false): Call<LecturesResponse> {
        return apiService.getPersonalLectures(force)
    }

    fun getLectureDetails(id: String, force: Boolean = false): Call<LectureDetailsResponse> {
        return apiService.getLectureDetails(id, force)
    }

    fun getLectureAppointments(id: String, force: Boolean = false): Call<LectureAppointmentsResponse> {
        return apiService.getLectureAppointments(id, force)
    }

    fun searchLectures(query: String): Call<LecturesResponse> {
        return apiService.searchLectures(query)
    }

    fun getPersonDetails(id: String, force: Boolean = false): Call<Employee> {
        return apiService.getPersonDetails(id, force)
    }

    fun searchPerson(query: String): Call<PersonList> {
        return apiService.searchPerson(query)
    }

    fun getGrades(force: Boolean = false): Call<ExamList> {
        return apiService.getGrades(force)
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
                    .addInterceptor(AddCacheControlInterceptor())
                    .addNetworkInterceptor(TUMOnlineInterceptor(context))
                    .cache(cache)
                    .build()

            // TODO: Add TypeConverter for date strings
            val tikXml = TikXml.Builder()
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