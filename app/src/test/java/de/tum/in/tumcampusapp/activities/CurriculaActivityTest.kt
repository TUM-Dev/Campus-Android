package de.tum.`in`.tumcampusapp.activities

import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class CurriculaActivityTest : BaseActivityTest() {

    override fun mainComponentDisplayedTest() {
        // idIsDisplayed(R.id.activity_curricula_list_view);
    }
}
