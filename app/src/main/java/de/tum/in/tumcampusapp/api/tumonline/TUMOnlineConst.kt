package de.tum.`in`.tumcampusapp.api.tumonline

import de.tum.`in`.tumcampusapp.api.tumonline.model.AccessToken
import de.tum.`in`.tumcampusapp.api.tumonline.model.TokenConfirmation
import de.tum.`in`.tumcampusapp.component.other.departments.model.OrgDetailItemList
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
import de.tum.`in`.tumcampusapp.utils.CacheManager

/**
 * Enum for all TUMOnline access possibilities
 */
class TUMOnlineConst<T> private constructor(private val webservice: String, val validity: Int, val response: Class<T>) {

    override fun toString(): String {
        return webservice
    }

    companion object {
        val CALENDER = TUMOnlineConst("kalender", CacheManager.VALIDITY_FIVE_DAYS, CalendarRowSet::class.java)
        val CREATE_EVENT = TUMOnlineConst("terminCreate", CacheManager.VALIDITY_DO_NOT_CACHE, CreateEvent::class.java)
        val DELETE_EVENT = TUMOnlineConst("terminDelete", CacheManager.VALIDITY_DO_NOT_CACHE, DeleteEvent::class.java)
        val TUITION_FEE_STATUS = TUMOnlineConst("studienbeitragsstatus", CacheManager.VALIDITY_TWO_DAYS, TuitionList::class.java)
        val LECTURES_PERSONAL = TUMOnlineConst("veranstaltungenEigene", CacheManager.VALIDITY_FIVE_DAYS, LecturesSearchRowSet::class.java)
        val LECTURES_DETAILS = TUMOnlineConst("veranstaltungenDetails", CacheManager.VALIDITY_TEN_DAYS, LectureDetailsRowSet::class.java)
        val LECTURES_APPOINTMENTS = TUMOnlineConst("veranstaltungenTermine", CacheManager.VALIDITY_TEN_DAYS, LectureAppointmentsRowSet::class.java)
        val LECTURES_SEARCH = TUMOnlineConst("veranstaltungenSuche", CacheManager.VALIDITY_DO_NOT_CACHE, LecturesSearchRowSet::class.java)
        val ORG_TREE = TUMOnlineConst("orgBaum", CacheManager.VALIDITY_ONE_MONTH, OrgItemList::class.java)
        val ORG_DETAILS = TUMOnlineConst("orgDetails", CacheManager.VALIDITY_ONE_MONTH, OrgDetailItemList::class.java)
        val PERSON_DETAILS = TUMOnlineConst("personenDetails", CacheManager.VALIDITY_FIVE_DAYS, Employee::class.java)
        val PERSON_SEARCH = TUMOnlineConst("personenSuche", CacheManager.VALIDITY_DO_NOT_CACHE, PersonList::class.java)
        val EXAMS = TUMOnlineConst("noten", CacheManager.VALIDITY_TEN_DAYS, ExamList::class.java)
        val TOKEN_CONFIRMED = TUMOnlineConst("isTokenConfirmed", CacheManager.VALIDITY_DO_NOT_CACHE, TokenConfirmation::class.java)
        val REQUEST_TOKEN = TUMOnlineConst("requestToken", CacheManager.VALIDITY_DO_NOT_CACHE, AccessToken::class.java)

        val IDENTITY = TUMOnlineConst("id", CacheManager.VALIDITY_DO_NOT_CACHE, IdentitySet::class.java)
        val SECRET_UPLOAD = TUMOnlineConst("secretUpload", CacheManager.VALIDITY_DO_NOT_CACHE, TokenConfirmation::class.java)
    }
}