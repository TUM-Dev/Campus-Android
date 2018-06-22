package de.tum.`in`.tumcampusapp.api.tumonline

import de.tum.`in`.tumcampusapp.api.tumonline.model.AccessToken
import de.tum.`in`.tumcampusapp.api.tumonline.model.TokenConfirmation
import de.tum.`in`.tumcampusapp.component.other.departments.model.OrgItemList
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarRowSet
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CreateEvent
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.DeleteEvent
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsRowSet
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureDetailsRowSet
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesSearchRowSet
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Employee
import de.tum.`in`.tumcampusapp.component.tumui.person.model.IdentitySet
import de.tum.`in`.tumcampusapp.component.tumui.person.model.PersonList
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.TuitionList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TUMOnlineAPIService {

    @GET("wbservicesbasic.kalender")
    fun getCalendar(@Query("pMonateVor") start: Int,
                    @Query("pMonateNach") end: Int): Call<CalendarRowSet>

    @GET("wbservicesbasic.terminCreate")
    fun createCalendarEvent(@Query("pTitel") title: String,
                            @Query("pAnmerkung") description: String,
                            @Query("pVon") start: String,
                            @Query("pBis") end: String,
                            @Query("pTerminNr") eventId: String? = null): Call<CreateEvent>

    @GET("wbservicesbasic.terminDelete")
    fun deleteCalendarEvent(@Query("pTerminNr") eventId: String): Call<DeleteEvent>

    @GET("wbservicesbasic.studienbeitragsstatus")
    fun getTuitionFeesStatus(): Call<TuitionList>

    @GET("wbservicesbasic.veranstaltungenEigene")
    fun getPersonalLectures(): Call<LecturesSearchRowSet>

    @GET("wbservicesbasic.veranstaltungenDetails")
    fun getLectureDetails(@Query("pLVNr") id: String): Call<LectureDetailsRowSet>

    @GET("wbservicesbasic.veranstaltungenTermine")
    fun getLectureAppointments(@Query("pLVNr") id: String): Call<LectureAppointmentsRowSet>

    @GET("wbservicesbasic.veranstaltungenSuche")
    fun searchLectures(@Query("pSuche") query: String): Call<LecturesSearchRowSet>

    @GET("wbservicesbasic.orgBaum")
    fun getOrgTree(): Call<OrgItemList>

    @GET("wbservicesbasic.personenDetails")
    fun getPersonDetails(@Query("pIdentNr") id: String): Call<Employee>

    @GET("wbservicesbasic.personenSuche")
    fun searchPerson(@Query("pSuche") query: String): Call<PersonList>

    @GET("wbservicesbasic.noten")
    fun getGrades(): Call<ExamList>

    @GET("wbservicesbasic.isTokenConfirmed")
    fun getTokenConfirmation(): Call<TokenConfirmation>

    @GET("wbservicesbasic.requestToken")
    fun requestToken(@Query("pUsername") username: String,
                     @Query("pTokenName") tokenName: String): Call<AccessToken>

    @GET("wbservicesbasic.id")
    fun getIdentity(): Call<IdentitySet>

    @GET("wbservicesbasic.secretUpload")
    fun uploadSecret(@Query("pToken") token: String,
                     @Query("pSecret") secret: String): Call<TokenConfirmation>

}