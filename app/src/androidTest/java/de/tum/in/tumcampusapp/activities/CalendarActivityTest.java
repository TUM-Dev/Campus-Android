package de.tum.in.tumcampusapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CalendarActivityTest extends BaseActivityTest {

    @Rule
    public ActivityTestRule<CalendarActivity> mActivityRule = new ActivityTestRule<>(CalendarActivity.class);

    @Override
    public void mainComponentDisplayedTest() {
        // TODO
    }
}
