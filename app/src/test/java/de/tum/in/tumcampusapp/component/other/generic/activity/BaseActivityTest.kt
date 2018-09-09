package de.tum.`in`.tumcampusapp.component.other.generic.activity

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
abstract class BaseActivityTest {

    @Test
    fun testDrawerLayout() {
        /* onView(withId(R.id.drawer_layout)).perform(open());
        onView(withId(R.id.left_drawer)).check(matches(isDisplayed()));
        onView(withId(R.id.drawer_layout)).perform(close());*/
    }

    @Test
    fun toolbarTest() {
        //  onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()));
    }

    @Test
    abstract fun mainComponentDisplayedTest()

}
