package de.tum.in.tumcampusapp.activities;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.tum.in.tumcampusapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RoomFinderActivityTest extends BaseActivityTest {

    @Rule
    public ActivityTestRule<RoomFinderActivity> mActivityRule = new ActivityTestRule<>(RoomFinderActivity.class);

    @Override
    @Test
    public void mainComponentDisplayedTest() {
        idIsDisplayed(R.id.list);
    }

    @Test
    public void searchTest() throws InterruptedException {
        // Adapted from https://android.googlesource.com/platform/frameworks/testing/+/android-support-test/espresso/sample/src/androidTest/java/android/support/test/testapp/ActionBarSearchActivityTest.java

        onView(allOf(withId(R.id.action_search), isDisplayed()))
                .perform(click());

        // App Compat SearchView widget does not use the same id as in the regular
        // android.widget.SearchView. R.id.search_src_text is the id created by appcompat
        // search widget.
        onView(withId(R.id.search_src_text))
                .perform(typeText("00.08.053\n"), closeSoftKeyboard());

        Thread.sleep(500);

        onView(withText("00.08.053, Seminarraum"))
                .check(matches(isDisplayed()));
    }
}