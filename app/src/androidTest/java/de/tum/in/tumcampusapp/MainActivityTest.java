package de.tum.in.tumcampusapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.tum.in.tumcampusapp.activities.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void toolbarTest() {
        onView(withId(R.id.main_toolbar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void mainComponentIsDisplayed() throws Exception {
        onView(withId(R.id.cards_view))
                .check(matches(isDisplayed()));

        onView(withText(R.string.swipe_instruction))
                .check(matches(isDisplayed()));
    }
}
