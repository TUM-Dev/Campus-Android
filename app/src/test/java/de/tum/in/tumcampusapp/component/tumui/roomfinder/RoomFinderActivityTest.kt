package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import de.tum.`in`.tumcampusapp.R
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomFinderActivityTest {

    @get:Rule
    val rule = ActivityTestRule(RoomFinderActivity::class.java)

    @Test
    fun testSearchWithExpectedResult() {
        onView(allOf(withId(R.id.action_search), isDisplayed()))
                .perform(click());

        // App Compat SearchView widget does not use the same id as in the regular
        // android.widget.SearchView. R.id.search_src_text is the id created by appcompat
        // search widget.
        onView(withId(R.id.search_src_text))
                .perform(typeText("00.08.053\n"), closeSoftKeyboard());

        Thread.sleep(500);

        onView(withText("00.08.053, Seminarraum"))
                .check(matches(isDisplayed()));
    }

}
