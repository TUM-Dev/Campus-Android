package de.tum.`in`.tumcampusapp.database.dao

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.other.locations.RoomLocationsDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
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
class RoomLocationsDaoTest {
    private var dao: RoomLocationsDao? = null
    private var calendarDao: CalendarDao? = null
    private var nr: Int = 0

    @Before
    fun setUp() {
        dao = TcaDb.getInstance(RuntimeEnvironment.application)
                .roomLocationsDao()
        calendarDao = TcaDb.getInstance(RuntimeEnvironment.application)
                .calendarDao()
        nr = 0
        JodaTimeAndroid.init(RuntimeEnvironment.application)
    }

    @After
    fun tearDown() {
        dao!!.flush()
        calendarDao!!.flush()
        TcaDb.getInstance(RuntimeEnvironment.application)
                .close()
    }

    private fun createCalendarItem(startDate: DateTime, location: String): CalendarItem {
        val item = CalendarItem(Integer.toString(nr),
                "good",
                "dummy url",
                "title " + Integer.toString(nr),
                "dummy description",
                startDate,
                startDate,
                location,
                false)
        nr++
        return item
    }

    /**
     * Get lecture coordinates when there's only one lecture
     * Expected output: expected room location is returned
     */
    @Test
    fun getNextLectureCoordinates() {
        val now = DateTime.now()
        calendarDao!!.insert(createCalendarItem(now.plusHours(1), "dummy location"))
        calendarDao!!.insert(createCalendarItem(now.plusHours(5), "some other location"))
        calendarDao!!.insert(createCalendarItem(now.plusHours(3), "dummy location"))
        calendarDao!!.insert(createCalendarItem(now.plusHours(2), "yet another location"))

        val expected = RoomLocations("dummy location", "123", "321")

        dao!!.insert(expected)
        dao!!.insert(RoomLocations("some other location", "456", "654"))
        dao!!.insert(RoomLocations("yet another location", "789", "987"))

        assertThat(dao!!.nextLectureCoordinates).isEqualToComparingFieldByField(expected)
    }
}
