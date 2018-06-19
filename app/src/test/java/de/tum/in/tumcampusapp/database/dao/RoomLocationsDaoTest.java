package de.tum.in.tumcampusapp.database.dao;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.TestApp;
import de.tum.in.tumcampusapp.component.other.locations.RoomLocationsDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarDao;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.RoomLocations;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.DateUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApp.class)
public class RoomLocationsDaoTest {
    private RoomLocationsDao dao;
    private CalendarDao calendarDao;
    private int nr;

    @Before
    public void setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).roomLocationsDao();
        calendarDao = TcaDb.getInstance(RuntimeEnvironment.application).calendarDao();
        nr = 0;
        JodaTimeAndroid.init(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        dao.flush();
        calendarDao.flush();
        TcaDb.getInstance(RuntimeEnvironment.application).close();
    }

    private CalendarItem createCalendarItem(DateTime startDate, String location) {
        CalendarItem item = new CalendarItem(Integer.toString(nr),
                                             "good",
                                             "dummy url",
                                             "title " + Integer.toString(nr),
                                             "dummy description",
                                             DateUtils.getDateTimeString(startDate.toDate()),
                                             DateUtils.getDateTimeString(startDate.toDate()),
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
