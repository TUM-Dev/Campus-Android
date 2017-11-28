package de.tum.in.tumcampusapp.activities;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CafeteriaActivityTest extends BaseActivityTest {

    @Rule
    public ActivityTestRule<CafeteriaActivity> mActivityRule = new ActivityTestRule<>(CafeteriaActivity.class);

    @Override
    public void mainComponentDisplayedTest() {
        //TODO
    }

    @Test
    public void optionsMenuTest() {
        //TODO
    }
}
