package de.tum.`in`.tumcampusapp.database.dao

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.other.locations.RoomLocationsDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.WidgetsTimetableBlacklistDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.WidgetsTimetableBlacklist
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocations
import de.tum.`in`.tumcampusapp.database.TcaDb
import net.danlew.android.joda.JodaTimeAndroid
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Ignore
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class CalendarDaoTest {

    private var dao: CalendarDao? = null
    private var wtbDao: WidgetsTimetableBlacklistDao? = null
    private var rlDao: RoomLocationsDao? = null
    private var nr: Int = 0

    @Before
    fun setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application)
                .calendarDao()
        wtbDao = TcaDb.getInstance(RuntimeEnvironment.application)
                .widgetsTimetableBlacklistDao()
        rlDao = TcaDb.getInstance(RuntimeEnvironment.application)
                .roomLocationsDao()
        nr = 0
        JodaTimeAndroid.init(RuntimeEnvironment.application)
    }

    @After
    fun tearDown() {
        dao!!.flush()
        wtbDao!!.flush()
        rlDao!!.flush()
        TcaDb.getInstance(RuntimeEnvironment.application)
                .close()
    }

    private fun createCalendarItem(status: String, startDate: DateTime, endDate: DateTime = startDate): CalendarItem {
        val item = CalendarItem(Integer.toString(nr),
                status,
                "dummy url",
                "title " + Integer.toString(nr),
                "dummy description",
                startDate,
                endDate,
                "dummy location",
                false)
        nr++
        return item
    }

    /**
     * Get all calendar events that don't have status as cancelled
     * Expected output: all items are returned as none of the statuses are CANCEL
     */
    @Test
    fun getAllNotCancelledAll() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("OPEN", now))
        dao!!.insert(createCalendarItem("OTHER", now))
        dao!!.insert(createCalendarItem("OTHER", now))
        dao!!.insert(createCalendarItem("OPEN", now))

        assertThat(dao!!.allNotCancelled).hasSize(4)
    }

    /**
     * Some calendar events have status CANCEL
     * Expected output: some items are returned as some of the statuses are CANCEL
     */
    @Test
    fun getAllNotCancelledSome() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("CANCEL", now))
        dao!!.insert(createCalendarItem("OTHER", now))
        dao!!.insert(createCalendarItem("CANCEL", now))
        dao!!.insert(createCalendarItem("OPEN", now))

        assertThat(dao!!.allNotCancelled).hasSize(2)
    }

    /**
     * Every calendar event has status CANCEL
     * Expected output: no items returned
     */
    @Test
    fun getAllNotCancelledNone() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("CANCEL", now))
        dao!!.insert(createCalendarItem("CANCEL", now))
        dao!!.insert(createCalendarItem("CANCEL", now))
        dao!!.insert(createCalendarItem("CANCEL", now))

        assertThat(dao!!.allNotCancelled).hasSize(0)
    }

    /**
     * Some dates are different
     * Expected output: some items are returned
     */
    @Test
    fun getAllByDateNotCancelledSomeDates() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GO", now.plusMonths(10)))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("OTHER", now.minusMonths(10)))
        dao!!.insert(createCalendarItem("COOL", now))

        assertThat(dao!!.getAllByDate(now)).hasSize(2)
    }

    /**
     * All dates are different
     * Expected output: no items are returned
     */
    @Test
    fun getAllByDateNotCancelledNoneDates() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GO", now.plusMonths(10)))
        dao!!.insert(createCalendarItem("OK", now.plusDays(123)))
        dao!!.insert(createCalendarItem("OTHER", now.minusMonths(10)))
        dao!!.insert(createCalendarItem("COOL", now.minusDays(123)))

        assertThat(dao!!.getAllByDate(now)).hasSize(0)
    }

    /**
     * A mix of statuses and dates
     * Expected output: some items are returned
     */
    @Test
    fun getAllByDateNotCancelledMix() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("CANCEL", now.minusDays(123)))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(123)))
        dao!!.insert(createCalendarItem("CANCEL", now))

        assertThat(dao!!.getAllByDate(now)).hasSize(2)
    }

    /**
     * All
     * Expected output: All items are returned
     */
    @Test
    fun getNextDaysAll() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.minusDays(3)))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(3)))
        dao!!.insert(createCalendarItem("YES", now))

        val from = now.minusDays(4)
        val to = now.plusDays(4)

        // widgetId is used only for blacklisting
        assertThat(dao!!.getNextDays(from, to, "1")).hasSize(4)
    }

    /**
     * some titles are matching with blacklists
     * Expected output: some items are returned
     */
    @Test
    fun getNextDaysSomeBlacklisted() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.minusDays(3)))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(3)))
        dao!!.insert(createCalendarItem("YES", now))

        val from = now.minusDays(4)
        val to = now.plusDays(4)

        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 0"))

        // widgetId is used only for blacklisting
        assertThat(dao!!.getNextDays(from, to, "1")).hasSize(3)
    }

    /**
     * some calendar items have outside date outside boundaries.
     * Expected output: some items are returned
     */
    @Test
    fun getNextDaysSomeOutOfDateRange() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.minusDays(3)))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(3)))
        dao!!.insert(createCalendarItem("YES", now))

        val from = now.minusDays(2)
        val to = now.plusDays(2)

        // widgetId is used only for blacklisting
        assertThat(dao!!.getNextDays(from, to, "1")).hasSize(2)
    }

    /**
     * Get all current lectures
     * Expected output: all items are returned
     */
    @Test
    fun getCurrentLecturesAll() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.minusDays(1), now.plusDays(1)))
        dao!!.insert(createCalendarItem("OK", now.minusHours(1), now.plusHours(1)))
        dao!!.insert(createCalendarItem("DUNNO", now.minusDays(5), now.plusHours(1)))
        dao!!.insert(createCalendarItem("YES", now.minusDays(1), now.plusDays(1)))

        assertThat(dao!!.currentLectures).hasSize(4)
    }

    /**
     * Get all current lectures
     * Expected output: all items are returned
     */
    @Test
    fun getCurrentLecturesSome() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusDays(1), now.plusDays(1)))
        dao!!.insert(createCalendarItem("OK", now.minusHours(1), now.plusHours(1)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(5), now.plusHours(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(1), now.plusDays(1)))

        assertThat(dao!!.currentLectures).hasSize(1)
    }

    /**
     * Get all current lectures
     * Expected output: all items are returned
     */
    @Test
    fun getCurrentLecturesNone() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusDays(1), now.plusDays(1)))
        dao!!.insert(createCalendarItem("OK", now.plusHours(1), now.plusHours(1)))
        dao!!.insert(createCalendarItem("DUNNO", now.minusDays(5), now.minusHours(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(1), now.plusDays(1)))

        assertThat(dao!!.currentLectures).hasSize(0)
    }

    /**
     * Several items  inserted
     * Expected output: true
     */
    @Test
    fun hasLectures() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now))
        dao!!.insert(createCalendarItem("YES", now))

        assertThat(dao!!.hasLectures()).isEqualTo(true)
    }

    /**
     * No items inserted
     * Expected output: false
     */
    @Test
    fun hasLecturesEmpty() {
        assertThat(dao!!.hasLectures()).isEqualTo(false)
    }

    /**
     * All items are without coordinates
     * Expected output: single item
     */
    @Test
    fun getLecturesWithoutCoordinates() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now))
        dao!!.insert(createCalendarItem("YES", now))

        rlDao!!.insert(RoomLocations("dummy location", "", ""))

        assertThat(dao!!.lecturesWithoutCoordinates).hasSize(1)
    }

    /**
     * All lectures have coordinates
     * Expected output: all items are returned
     */
    @Test
    fun getLecturesWithoutCoordinatesNone() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("DUNNO", now))
        dao!!.insert(createCalendarItem("YES", now))

        rlDao!!.insert(RoomLocations("dummy location", "coordinate", "coordinate"))

        assertThat(dao!!.lecturesWithoutCoordinates).hasSize(0)
    }

    /**
     * Get all next items
     * Expected output: all items are returned
     */
    @Test
    fun getNextCalendarItem() {
        val now = DateTime.now()
        val expected = createCalendarItem("GOOD", now.plusHours(1))
        dao!!.insert(expected)
        dao!!.insert(createCalendarItem("OK", now.plusDays(5)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(2)))

        val results = dao!!.nextCalendarItems
        assertThat(results).hasSize(1)
        assertThat(results[0]).isEqualToComparingFieldByField(expected)
    }

    /**
     * Get all calendar items that are blacklisted
     * Expected output: all items are returned
     */
    @Test
    fun getLecturesWithBlacklist() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusHours(1)))
        dao!!.insert(createCalendarItem("OK", now.plusDays(5)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(2)))

        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 0"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 1"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 2"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 3"))

        assertThat(dao!!.getLecturesInBlacklist("1")).hasSize(4)
    }

    /**
     * Some titles are blacklisted
     * Expected output: some items are returned
     */
    @Test
    fun getLecturesWithBlacklistSomeTitles() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusHours(1)))
        dao!!.insert(createCalendarItem("OK", now.plusDays(5)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(2)))

        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 0"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 1"))

        assertThat(dao!!.getLecturesInBlacklist("1")).hasSize(2)
    }

    /**
     * All titles are blacklisted, but on different widget ids
     * Expected output: some items are returned
     */
    @Test
    fun getLecturesWithBlacklistSomeWidget() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusHours(1)))
        dao!!.insert(createCalendarItem("OK", now.plusDays(5)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(2)))

        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 0"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(1, "title 1"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 2"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 3"))

        assertThat(dao!!.getLecturesInBlacklist("1")).hasSize(2)
    }

    /**
     * Nothing is blacklisted
     * Expected output: empty list is returned
     */
    @Test
    fun getLecturesWithBlacklistNone() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusHours(1)))
        dao!!.insert(createCalendarItem("OK", now.plusDays(5)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(2)))

        assertThat(dao!!.getLecturesInBlacklist("1")).hasSize(0)
    }

    /**
     * Everything is blacklisted, but for different widget id
     * Expected output: empty list is returned
     */
    @Test
    fun getLecturesWithBlacklistNoneWidget() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now.plusHours(1)))
        dao!!.insert(createCalendarItem("OK", now.plusDays(5)))
        dao!!.insert(createCalendarItem("DUNNO", now.plusDays(1)))
        dao!!.insert(createCalendarItem("YES", now.plusDays(2)))

        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 0"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 1"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 2"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 3"))

        assertThat(dao!!.getLecturesInBlacklist("1")).hasSize(0)
    }

    /**
     * Get lectures not in blacklist, when none of them are
     * Expected output: all are returned
     */
    @Test
    fun getLecturesNotInBlacklist() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("GOOD", now))
        dao!!.insert(createCalendarItem("GOOD", now))
        dao!!.insert(createCalendarItem("GOOD", now))

        assertThat(dao!!.getLecturesNotInBlacklist("1")).hasSize(3)
    }

    /**
     * Some are in blacklist
     * Expected output: rest are returned
     */
    @Test
    fun getLecturesNotInBlacklistSome() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("BAD", now))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("YES", now))
        dao!!.insert(createCalendarItem("YES", now))

        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 0"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 1"))

        assertThat(dao!!.getLecturesNotInBlacklist("2")).hasSize(2)
    }

    /**
     * All are in blacklist
     * Expected output: Empty list
     */
    @Test
    fun getLecturesNotInBlacklistNone() {
        val now = DateTime.now()
        dao!!.insert(createCalendarItem("BAD", now))
        dao!!.insert(createCalendarItem("OK", now))
        dao!!.insert(createCalendarItem("YES", now))
        dao!!.insert(createCalendarItem("YES", now))

        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 0"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 1"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 2"))
        wtbDao!!.insert(WidgetsTimetableBlacklist(2, "title 3"))

        assertThat(dao!!.getLecturesNotInBlacklist("2")).hasSize(0)
    }

}
