package de.tum.`in`.tumcampusapp.component.ui.overview

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivityTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class MainActivityTest : BaseActivityTest() {

    @Test
    override fun mainComponentDisplayedTest() {
        //idIsDisplayed(R.id.cards_view);

        //onView(withText(R.string.swipe_instruction)).check(matches(isDisplayed()));
    }

}
