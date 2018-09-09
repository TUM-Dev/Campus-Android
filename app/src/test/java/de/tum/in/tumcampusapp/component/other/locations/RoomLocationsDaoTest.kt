package de.tum.`in`.tumcampusapp.component.other.locations

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarDao
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocations
import de.tum.`in`.tumcampusapp.database.TcaDb
import net.danlew.android.joda.JodaTimeAndroid
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomLocationsDaoTest {

    private lateinit var database: TcaDb

    private val roomLocationsDao: RoomLocationsDao by lazy {
        database.roomLocationsDao()
    }

    private val calendarDao: CalendarDao by lazy {
        database.calendarDao()
    }

    private var nr: Int = 0

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), TcaDb::class.java)
                .allowMainThreadQueries()
                .build()
        nr = 0
        JodaTimeAndroid.init(InstrumentationRegistry.getContext())
    }

    @After
    fun tearDown() {
        database.close()
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
        calendarDao.insert(createCalendarItem(now.plusHours(1), "dummy location"))
        calendarDao.insert(createCalendarItem(now.plusHours(5), "some other location"))
        calendarDao.insert(createCalendarItem(now.plusHours(3), "dummy location"))
        calendarDao.insert(createCalendarItem(now.plusHours(2), "yet another location"))

        val expected = RoomLocations("dummy location", "123", "321")

        roomLocationsDao.insert(expected)
        roomLocationsDao.insert(RoomLocations("some other location", "456", "654"))
        roomLocationsDao.insert(RoomLocations("yet another location", "789", "987"))

        assertThat(roomLocationsDao.nextLectureCoordinates).isEqualToComparingFieldByField(expected)
    }
}
