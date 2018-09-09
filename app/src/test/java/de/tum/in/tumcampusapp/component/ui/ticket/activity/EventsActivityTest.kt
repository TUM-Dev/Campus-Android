package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.swipeLeft
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventsActivityTest {

    @get:Rule
    val rule = ActivityTestRule(EventsActivity::class.java)

    @Test
    fun testAssertBookingsPlaceholderIfNoBookings() {
        onView(withId(R.id.viewPager))
                .check(matches(isDisplayed()))

        onView(withId(R.id.viewPager))
                .perform(swipeLeft())

        onView(withId(R.id.placeholderTextView))
                .check(matches(isDisplayed()))
    }

}