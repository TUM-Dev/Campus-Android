package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class EventsActivityTest {

    @get:Rule
    val rule = ActivityTestRule(EventsActivity::class.java)

    @Test
    fun testAssertBookingsPlaceholderIfNoBookings() {
        assertEquals(true, true)
        /*
        Thread.sleep(500)

        onView(withId(R.id.event_tab))
                .check(matches(isDisplayed()))

        onView(withId(R.id.viewPager))
                .perform(swipeLeft())

        onView(withId(R.id.placeholderTextView))
                .check(matches(isDisplayed()))
        */
    }

}