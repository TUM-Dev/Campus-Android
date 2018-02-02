package de.tum.in.tumcampusapp.activities;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.runner.RunWith;

import de.tum.in.tumcampusapp.R;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class KinoActivityTest extends BaseActivityTest {

    @Rule
    public ActivityTestRule<KinoActivity> mActivityRule = new ActivityTestRule<>(KinoActivity.class);

    @Override
    public void mainComponentDisplayedTest() {
        idIsDisplayed(R.id.no_movies_layout);
        // TODO: download all from external, then check if movies are shown
    }
}
