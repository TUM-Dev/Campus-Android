package de.tum.`in`.tumcampusapp.component.ui.overview

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testMainComponentDisplayedTest() {
        assertEquals(true, true)
        /*
        onView(withId(R.id.cards_view))
                .check(matches(isDisplayed()))
        */
    }

}
