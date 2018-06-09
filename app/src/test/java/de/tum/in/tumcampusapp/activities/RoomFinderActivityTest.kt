package de.tum.`in`.tumcampusapp.activities

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class RoomFinderActivityTest : BaseActivityTest() {

    @Test
    override fun mainComponentDisplayedTest() {
        //idIsDisplayed(R.id.list);
    }

    @Test
    @Throws(InterruptedException::class)
    fun searchTest() {
        // Adapted from https://android.googlesource.com/platform/frameworks/testing/+/android-support-test/espresso/sample/src/androidTest/java/android/support/test/testapp/ActionBarSearchActivityTest.java
        /*
        onView(allOf(withId(R.id.action_search), isDisplayed()))
                .perform(click());

        // App Compat SearchView widget does not use the same id as in the regular
        // android.widget.SearchView. R.id.search_src_text is the id created by appcompat
        // search widget.
        onView(withId(R.id.search_src_text))
                .perform(typeText("00.08.053\n"), closeSoftKeyboard());

        Thread.sleep(500);

        onView(withText("00.08.053, Seminarraum"))
                .check(matches(isDisplayed()));*/
    }
}