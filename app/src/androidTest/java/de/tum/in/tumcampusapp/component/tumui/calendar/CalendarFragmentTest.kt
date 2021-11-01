package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.settings.SettingsFragment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarFragmentTest {

    @Test
    fun calendarFragment_shouldPersistWeekMode() {
        //Utils.getSettingBool(instrumentationContext, Const.CALENDAR_WEEK_MODE, false)

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchFragmentInContainer<SettingsFragment>(Bundle.EMPTY, R.style.AppTheme)
        Thread.sleep(5000)
        assertEquals("me.lindenbauer.recipetracker", appContext.packageName)


        // WHEN User is in the calendar fragment
        //val scenario = launchFragmentInContainer<CafeteriaDetailsSectionFragment>(Bundle(), R.style.AppTheme)
        //val scenario = launchActivity<CalendarActivity>()
        //openActionBarOverflowOrOptionsMenu(instrumentationContext)
        Log.d("CalTest", "foo")

        // scenario.onFragment { fragment -> fragment.onOptionsItemSelected(Menu)}
        //THEN
        //onView(withId(R.id.action_switch_view_mode)).check(matches(isDisplayed()))
        //onView(withId(R.id.action_switch_view_mode)).perform(click())
        //Thread.sleep(2000)
        //assertEquals(4, 2+2)
    }

}