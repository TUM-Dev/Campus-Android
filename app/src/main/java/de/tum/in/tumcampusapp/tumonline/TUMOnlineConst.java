package de.tum.in.tumcampusapp.tumonline;

import de.tum.in.tumcampusapp.models.AccessToken;
import de.tum.in.tumcampusapp.models.CalendarRowSet;
import de.tum.in.tumcampusapp.models.Employee;
import de.tum.in.tumcampusapp.models.IdentitySet;
import de.tum.in.tumcampusapp.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampusapp.models.LectureDetailsRowSet;
import de.tum.in.tumcampusapp.models.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.models.OrgDetailItemList;
import de.tum.in.tumcampusapp.models.OrgItemList;
import de.tum.in.tumcampusapp.models.PersonList;
import de.tum.in.tumcampusapp.models.TokenConfirmation;
import de.tum.in.tumcampusapp.models.TuitionList;
import de.tum.in.tumcampusapp.models.managers.CacheManager;

/**
 * Enum for all TUMOnline access possibilities
 */
public class TUMOnlineConst<T> {
    public static final TUMOnlineConst<CalendarRowSet> CALENDER = new TUMOnlineConst<>("kalender", CacheManager.VALIDITY_FIFE_DAYS, CalendarRowSet.class);
    public static final TUMOnlineConst<TuitionList> TUITION_FEE_STATUS = new TUMOnlineConst<>("studienbeitragsstatus", CacheManager.VALIDITY_TWO_DAYS, TuitionList.class);
    public static final TUMOnlineConst<LecturesSearchRowSet> LECTURES_PERSONAL = new TUMOnlineConst<>("veranstaltungenEigene", CacheManager.VALIDITY_FIFE_DAYS, LecturesSearchRowSet.class);
    public static final TUMOnlineConst<LectureDetailsRowSet> LECTURES_DETAILS = new TUMOnlineConst<>("veranstaltungenDetails", CacheManager.VALIDITY_TEN_DAYS, LectureDetailsRowSet.class);
    public static final TUMOnlineConst<LectureAppointmentsRowSet> LECTURES_APPOINTMENTS = new TUMOnlineConst<>("veranstaltungenTermine", CacheManager.VALIDITY_TEN_DAYS, LectureAppointmentsRowSet.class);
    public static final TUMOnlineConst<LecturesSearchRowSet> LECTURES_SEARCH = new TUMOnlineConst<>("veranstaltungenSuche", CacheManager.VALIDITY_DO_NOT_CACHE, LecturesSearchRowSet.class);
    public static final TUMOnlineConst<OrgItemList> ORG_TREE = new TUMOnlineConst<>("orgBaum", CacheManager.VALIDITY_ONE_MONTH, OrgItemList.class);
    public static final TUMOnlineConst<OrgDetailItemList> ORG_DETAILS = new TUMOnlineConst<>("orgDetails", CacheManager.VALIDITY_ONE_MONTH, OrgDetailItemList.class);
    public static final TUMOnlineConst<Employee> PERSON_DETAILS = new TUMOnlineConst<>("personenDetails", CacheManager.VALIDITY_FIFE_DAYS, Employee.class);
    public static final TUMOnlineConst<PersonList> PERSON_SEARCH = new TUMOnlineConst<>("personenSuche", CacheManager.VALIDITY_DO_NOT_CACHE, PersonList.class);
    public static final TUMOnlineConst<TokenConfirmation> TOKEN_CONFIRMED = new TUMOnlineConst<>("isTokenConfirmed", CacheManager.VALIDITY_DO_NOT_CACHE, TokenConfirmation.class);
    public static final TUMOnlineConst<AccessToken> REQUEST_TOKEN = new TUMOnlineConst<>("requestToken", CacheManager.VALIDITY_DO_NOT_CACHE, AccessToken.class);
    public static final TUMOnlineConst<IdentitySet> IDENTITY = new TUMOnlineConst<>("id", CacheManager.VALIDITY_DO_NOT_CACHE, IdentitySet.class);

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

    public String toString() {
        return webservice;
    }
}