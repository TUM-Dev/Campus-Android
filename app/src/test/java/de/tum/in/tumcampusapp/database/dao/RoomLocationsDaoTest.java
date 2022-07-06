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

import androidx.test.core.app.ApplicationProvider;
import de.tum.in.tumcampusapp.TestApp;
import de.tum.in.tumcampusapp.component.other.locations.RoomLocationsDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.RoomLocations;
import de.tum.in.tumcampusapp.database.TcaDb;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class RoomLocationsDaoTest {
    private RoomLocationsDao dao;
    private CalendarDao calendarDao;
    private int nr;

    @Before
    public void setUp() {
        dao = TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                             .roomLocationsDao();
        calendarDao = TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                                     .calendarDao();
        nr = 0;
        JodaTimeAndroid.init(ApplicationProvider.getApplicationContext());
    }

    @After
    public void tearDown() {
        dao.flush();
        calendarDao.flush();
        TcaDb.Companion.getInstance(ApplicationProvider.getApplicationContext())
                       .close();
    }

    private CalendarItem createCalendarItem(DateTime startDate, String location) {
        CalendarItem item = new CalendarItem(Integer.toString(nr),
                                             "good",
                                             "dummy url",
                                             "title " + nr,
                                             "dummy description",
                                             startDate,
                                             startDate,
                                             location,
                                             false);
        nr++;
        return item;
    }

    /**
     * Get lecture coordinates when there's only one lecture
     * Expected output: expected room location is returned
     */
    @Test
    public void getNextLectureCoordinates() {
        DateTime now = DateTime.now();
        calendarDao.insert(createCalendarItem(now.plusHours(1), "dummy location"));
        calendarDao.insert(createCalendarItem(now.plusHours(5), "some other location"));
        calendarDao.insert(createCalendarItem(now.plusHours(3), "dummy location"));
        calendarDao.insert(createCalendarItem(now.plusHours(2), "yet another location"));

        RoomLocations expected = new RoomLocations("dummy location", "123", "321");

        dao.insert(expected);
        dao.insert(new RoomLocations("some other location", "456", "654"));
        dao.insert(new RoomLocations("yet another location", "789", "987"));

        assertThat(dao.getNextLectureCoordinates()).isEqualToComparingFieldByField(expected);
    }
}
