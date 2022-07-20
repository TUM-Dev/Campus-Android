package de.tum.in.tumcampusapp.database.dao;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import androidx.test.core.app.ApplicationProvider;
import de.tum.in.tumcampusapp.TestApp;
import de.tum.in.tumcampusapp.component.other.locations.RoomLocationsDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.WidgetsTimetableBlacklistDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.RoomLocations;
import de.tum.in.tumcampusapp.database.TcaDb;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class CalendarDaoTest {
    private CalendarDao dao;
    private WidgetsTimetableBlacklistDao wtbDao;
    private RoomLocationsDao rlDao;
    private int nr;

    @Before
    public void setUp() {
        dao = TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                             .calendarDao();
        wtbDao = TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                                .widgetsTimetableBlacklistDao();
        rlDao = TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                               .roomLocationsDao();
        nr = 0;
        JodaTimeAndroid.init(ApplicationProvider.getApplicationContext());
    }

    @After
    public void tearDown() {
        dao.flush();
        wtbDao.flush();
        rlDao.flush();
        TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                       .close();
    }

    private CalendarItem createCalendarItem(String status, DateTime startDate) {
        return createCalendarItem(status, startDate, startDate);
    }

    private CalendarItem createCalendarItem(String status, DateTime startDate, DateTime endDate) {
        CalendarItem item = new CalendarItem(Integer.toString(nr),
                                             status,
                                             "dummy url",
                                             "title " + nr,
                                             "dummy description",
                                             startDate,
                                             endDate,
                                             "dummy location",
                                             false);
        nr++;
        return item;
    }

    /**
     * Get all calendar events that don't have status as cancelled
     * Expected output: all items are returned as none of the statuses are CANCEL
     */
    @Test
    public void getAllNotCancelledAll() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("OPEN", now));
        dao.insert(createCalendarItem("OTHER", now));
        dao.insert(createCalendarItem("OTHER", now));
        dao.insert(createCalendarItem("OPEN", now));

        assertThat(dao.getAllNotCancelled()).hasSize(4);
    }

    /**
     * Some calendar events have status CANCEL
     * Expected output: some items are returned as some of the statuses are CANCEL
     */
    @Test
    public void getAllNotCancelledSome() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("CANCEL", now));
        dao.insert(createCalendarItem("OTHER", now));
        dao.insert(createCalendarItem("CANCEL", now));
        dao.insert(createCalendarItem("OPEN", now));

        assertThat(dao.getAllNotCancelled()).hasSize(2);
    }

    /**
     * Every calendar event has status CANCEL
     * Expected output: no items returned
     */
    @Test
    public void getAllNotCancelledNone() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("CANCEL", now));
        dao.insert(createCalendarItem("CANCEL", now));
        dao.insert(createCalendarItem("CANCEL", now));
        dao.insert(createCalendarItem("CANCEL", now));

        assertThat(dao.getAllNotCancelled()).hasSize(0);
    }

    /**
     * Some dates are different
     * Expected output: some items are returned
     */
    @Test
    public void getAllByDateNotCancelledSomeDates() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GO", now.plusMonths(10)));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("OTHER", now.minusMonths(10)));
        dao.insert(createCalendarItem("COOL", now));

        assertThat(dao.getAllByDate(now)).hasSize(2);
    }

    /**
     * All dates are different
     * Expected output: no items are returned
     */
    @Test
    public void getAllByDateNotCancelledNoneDates() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GO", now.plusMonths(10)));
        dao.insert(createCalendarItem("OK", now.plusDays(123)));
        dao.insert(createCalendarItem("OTHER", now.minusMonths(10)));
        dao.insert(createCalendarItem("COOL", now.minusDays(123)));

        assertThat(dao.getAllByDate(now)).hasSize(0);
    }

    /**
     * A mix of statuses and dates
     * Expected output: some items are returned
     */
    @Test
    public void getAllByDateNotCancelledMix() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("CANCEL", now.minusDays(123)));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(123)));
        dao.insert(createCalendarItem("CANCEL", now));

        assertThat(dao.getAllByDate(now)).hasSize(2);
    }

    /**
     * All
     * Expected output: All items are returned
     */
    @Test
    public void getNextDaysAll() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.minusDays(3)));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(3)));
        dao.insert(createCalendarItem("YES", now));

        DateTime from = now.minusDays(4);
        DateTime to = now.plusDays(4);

        // widgetId is used only for blacklisting
        assertThat(dao.getNextDays(from, to, "1")).hasSize(4);
    }

    /**
     * some titles are matching with blacklists
     * Expected output: some items are returned
     */
    @Test
    public void getNextDaysSomeBlacklisted() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.minusDays(3)));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(3)));
        dao.insert(createCalendarItem("YES", now));

        DateTime from = now.minusDays(4);
        DateTime to = now.plusDays(4);

        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 0"));

        // widgetId is used only for blacklisting
        assertThat(dao.getNextDays(from, to, "1")).hasSize(3);
    }

    /**
     * some calendar items have outside date outside boundaries.
     * Expected output: some items are returned
     */
    @Test
    public void getNextDaysSomeOutOfDateRange() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.minusDays(3)));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(3)));
        dao.insert(createCalendarItem("YES", now));

        DateTime from = now.minusDays(2);
        DateTime to = now.plusDays(2);

        // widgetId is used only for blacklisting
        assertThat(dao.getNextDays(from, to, "1")).hasSize(2);
    }

    /**
     * Get all current lectures
     * Expected output: all items are returned
     */
    @Test
    public void getCurrentLecturesAll() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.minusDays(1), now.plusDays(1)));
        dao.insert(createCalendarItem("OK", now.minusHours(1), now.plusHours(1)));
        dao.insert(createCalendarItem("DUNNO", now.minusDays(5), now.plusHours(1)));
        dao.insert(createCalendarItem("YES", now.minusDays(1), now.plusDays(1)));

        assertThat(dao.getCurrentLectures()).hasSize(4);
    }

    /**
     * Get all current lectures
     * Expected output: all items are returned
     */
    @Test
    public void getCurrentLecturesSome() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusDays(1), now.plusDays(1)));
        dao.insert(createCalendarItem("OK", now.minusHours(1), now.plusHours(1)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(5), now.plusHours(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(1), now.plusDays(1)));

        assertThat(dao.getCurrentLectures()).hasSize(1);
    }

    /**
     * Get all current lectures
     * Expected output: all items are returned
     */
    @Test
    public void getCurrentLecturesNone() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusDays(1), now.plusDays(1)));
        dao.insert(createCalendarItem("OK", now.plusHours(1), now.plusHours(1)));
        dao.insert(createCalendarItem("DUNNO", now.minusDays(5), now.minusHours(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(1), now.plusDays(1)));

        assertThat(dao.getCurrentLectures()).hasSize(0);
    }

    /**
     * Several items  inserted
     * Expected output: true
     */
    @Test
    public void hasLectures() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now));
        dao.insert(createCalendarItem("YES", now));

        assertThat(dao.hasLectures()).isEqualTo(true);
    }

    /**
     * No items inserted
     * Expected output: false
     */
    @Test
    public void hasLecturesEmpty() {
        assertThat(dao.hasLectures()).isEqualTo(false);
    }

    /**
     * All items are without coordinates
     * Expected output: single item
     */
    @Test
    public void getLecturesWithoutCoordinates() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now));
        dao.insert(createCalendarItem("YES", now));

        rlDao.insert(new RoomLocations("dummy location", "", ""));

        assertThat(dao.getLecturesWithoutCoordinates()).hasSize(1);
    }

    /**
     * All lectures have coordinates
     * Expected output: all items are returned
     */
    @Test
    public void getLecturesWithoutCoordinatesNone() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("DUNNO", now));
        dao.insert(createCalendarItem("YES", now));

        rlDao.insert(new RoomLocations("dummy location", "coordinate", "coordinate"));

        assertThat(dao.getLecturesWithoutCoordinates()).hasSize(0);
    }

    /**
     * Get all next items
     * Expected output: all items are returned
     */
    @Test
    public void getNextCalendarItem() {
        DateTime now = DateTime.now();
        CalendarItem expected = createCalendarItem("GOOD", now.plusHours(1));
        dao.insert(expected);
        dao.insert(createCalendarItem("OK", now.plusDays(5)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(2)));

        List<CalendarItem> results = dao.getNextCalendarItems();
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualToComparingFieldByField(expected);
    }

    /**
     * Get all calendar items that are blacklisted
     * Expected output: all items are returned
     */
    @Test
    public void getLecturesWithBlacklist() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusHours(1)));
        dao.insert(createCalendarItem("OK", now.plusDays(5)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(2)));

        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 0"));
        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 1"));
        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 2"));
        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 3"));

        assertThat(dao.getLecturesInBlacklist("1")).hasSize(4);
    }

    /**
     * Some titles are blacklisted
     * Expected output: some items are returned
     */
    @Test
    public void getLecturesWithBlacklistSomeTitles() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusHours(1)));
        dao.insert(createCalendarItem("OK", now.plusDays(5)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(2)));

        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 0"));
        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 1"));

        assertThat(dao.getLecturesInBlacklist("1")).hasSize(2);
    }

    /**
     * All titles are blacklisted, but on different widget ids
     * Expected output: some items are returned
     */
    @Test
    public void getLecturesWithBlacklistSomeWidget() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusHours(1)));
        dao.insert(createCalendarItem("OK", now.plusDays(5)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(2)));

        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 0"));
        wtbDao.insert(new WidgetsTimetableBlacklist(1, "title 1"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 2"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 3"));

        assertThat(dao.getLecturesInBlacklist("1")).hasSize(2);
    }

    /**
     * Nothing is blacklisted
     * Expected output: empty list is returned
     */
    @Test
    public void getLecturesWithBlacklistNone() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusHours(1)));
        dao.insert(createCalendarItem("OK", now.plusDays(5)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(2)));

        assertThat(dao.getLecturesInBlacklist("1")).hasSize(0);
    }

    /**
     * Everything is blacklisted, but for different widget id
     * Expected output: empty list is returned
     */
    @Test
    public void getLecturesWithBlacklistNoneWidget() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now.plusHours(1)));
        dao.insert(createCalendarItem("OK", now.plusDays(5)));
        dao.insert(createCalendarItem("DUNNO", now.plusDays(1)));
        dao.insert(createCalendarItem("YES", now.plusDays(2)));

        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 0"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 1"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 2"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 3"));

        assertThat(dao.getLecturesInBlacklist("1")).hasSize(0);
    }

    /**
     * Get lectures not in blacklist, when none of them are
     * Expected output: all are returned
     */
    @Test
    public void getLecturesNotInBlacklist() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("GOOD", now));
        dao.insert(createCalendarItem("GOOD", now));
        dao.insert(createCalendarItem("GOOD", now));

        assertThat(dao.getLecturesNotInBlacklist("1")).hasSize(3);
    }

    /**
     * Some are in blacklist
     * Expected output: rest are returned
     */
    @Test
    public void getLecturesNotInBlacklistSome() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("BAD", now));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("YES", now));
        dao.insert(createCalendarItem("YES", now));

        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 0"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 1"));

        assertThat(dao.getLecturesNotInBlacklist("2")).hasSize(2);
    }

    /**
     * All are in blacklist
     * Expected output: Empty list
     */
    @Test
    public void getLecturesNotInBlacklistNone() {
        DateTime now = DateTime.now();
        dao.insert(createCalendarItem("BAD", now));
        dao.insert(createCalendarItem("OK", now));
        dao.insert(createCalendarItem("YES", now));
        dao.insert(createCalendarItem("YES", now));

        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 0"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 1"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 2"));
        wtbDao.insert(new WidgetsTimetableBlacklist(2, "title 3"));

        assertThat(dao.getLecturesNotInBlacklist("2")).hasSize(0);
    }
}
