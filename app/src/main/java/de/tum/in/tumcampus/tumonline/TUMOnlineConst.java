package de.tum.in.tumcampus.tumonline;

import de.tum.in.tumcampus.models.AccessToken;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.Employee;
import de.tum.in.tumcampus.models.ExamList;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.models.LectureDetailsRowSet;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.models.OrgDetailItemList;
import de.tum.in.tumcampus.models.OrgItemList;
import de.tum.in.tumcampus.models.PersonList;
import de.tum.in.tumcampus.models.TokenConfirmation;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.models.managers.CacheManager;

/**
 * Enum for all TUMOnline access possibilities
 */
public class TUMOnlineConst<T> {
    public static final TUMOnlineConst<CalendarRowSet> CALENDER = new TUMOnlineConst<CalendarRowSet>("kalender", CacheManager.VALIDITY_FIFE_DAYS, CalendarRowSet.class);
    public static final TUMOnlineConst<TuitionList> TUITION_FEE_STATUS = new TUMOnlineConst<TuitionList>("studienbeitragsstatus", CacheManager.VALIDITY_TWO_DAYS, TuitionList.class);
    public static final TUMOnlineConst<LecturesSearchRowSet> LECTURES_PERSONAL = new TUMOnlineConst<LecturesSearchRowSet>("veranstaltungenEigene", CacheManager.VALIDITY_FIFE_DAYS, LecturesSearchRowSet.class);
    public static final TUMOnlineConst<LectureDetailsRowSet> LECTURES_DETAILS = new TUMOnlineConst<LectureDetailsRowSet>("veranstaltungenDetails", CacheManager.VALIDITY_TEN_DAYS, LectureDetailsRowSet.class);
    public static final TUMOnlineConst<LectureAppointmentsRowSet> LECTURES_APPOINTMENTS = new TUMOnlineConst<LectureAppointmentsRowSet>("veranstaltungenTermine", CacheManager.VALIDITY_TEN_DAYS, LectureAppointmentsRowSet.class);
    public static final TUMOnlineConst<LecturesSearchRowSet> LECTURES_SEARCH = new TUMOnlineConst<LecturesSearchRowSet>("veranstaltungenSuche", CacheManager.VALIDITY_DO_NOT_CACHE, LecturesSearchRowSet.class);
    public static final TUMOnlineConst<OrgItemList> ORG_TREE = new TUMOnlineConst<OrgItemList>("orgBaum", CacheManager.VALIDITY_ONE_MONTH, OrgItemList.class);
    public static final TUMOnlineConst<OrgDetailItemList> ORG_DETAILS = new TUMOnlineConst<OrgDetailItemList>("orgDetails", CacheManager.VALIDITY_ONE_MONTH, OrgDetailItemList.class);
    public static final TUMOnlineConst<Employee> PERSON_DETAILS = new TUMOnlineConst<Employee>("personenDetails", CacheManager.VALIDITY_DO_NOT_CACHE, Employee.class);
    public static final TUMOnlineConst<PersonList> PERSON_SEARCH = new TUMOnlineConst<PersonList>("personenSuche", CacheManager.VALIDITY_DO_NOT_CACHE, PersonList.class);
    public static final TUMOnlineConst<ExamList> EXAMS = new TUMOnlineConst<ExamList>("noten", CacheManager.VALIDITY_TEN_DAYS, ExamList.class);
    public static final TUMOnlineConst<TokenConfirmation> TOKEN_CONFIRMED = new TUMOnlineConst<TokenConfirmation>("isTokenConfirmed", CacheManager.VALIDITY_DO_NOT_CACHE, TokenConfirmation.class);
    public static final TUMOnlineConst<AccessToken> REQUEST_TOKEN = new TUMOnlineConst<AccessToken>("requestToken", CacheManager.VALIDITY_DO_NOT_CACHE, AccessToken.class);
    
    private final String webservice;
    private final int validity;

    private final Class<T> response;
    private TUMOnlineConst(final String s, final int v, final Class<T> r) {
        webservice = s;
        validity = v;
        response = r;
    }
    
    public int getValidity() {
        return validity;
    }

    public Class<T> getResponse() {
        return response;
    }
    
    public String toString() { return webservice; }
}