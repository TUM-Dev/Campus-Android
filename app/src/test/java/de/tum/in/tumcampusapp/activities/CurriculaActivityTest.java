package de.tum.in.tumcampusapp.activities;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.TestApp;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApp.class)
public class CurriculaActivityTest extends BaseActivityTest {

    @Override
    public void mainComponentDisplayedTest() {
        // idIsDisplayed(R.id.activity_curricula_list_view);
    }
}
