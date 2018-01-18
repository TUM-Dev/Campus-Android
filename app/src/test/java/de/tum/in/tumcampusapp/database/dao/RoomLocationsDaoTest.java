package de.tum.in.tumcampusapp.database.dao;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.models.dbEntities.RoomLocations;
import de.tum.in.tumcampusapp.models.tumo.CalendarItem;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RoomLocationsDaoTest {
    private RoomLocationsDao dao;
    private CalendarDao calendarDao;
    private int nr;

    @Before
    public void setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application).roomLocationsDao();
        calendarDao = TcaDb.getInstance(RuntimeEnvironment.application).calendarDao();
        nr = 0;
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
                                             Utils.getDateTimeString(startDate.toDate()),
                                             Utils.getDateTimeString(startDate.toDate()),
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
