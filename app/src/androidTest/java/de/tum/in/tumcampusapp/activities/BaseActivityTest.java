package de.tum.in.tumcampusapp.activities;

import org.junit.Test;

import de.tum.in.tumcampusapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.close;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public abstract class BaseActivityTest {

    @Test
    public void testDrawerLayout() {
        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withId(R.id.left_drawer)).check(matches(isDisplayed()));
        onView(withId(R.id.drawer_layout)).perform(close());
    }

    @Test
    public void toolbarTest() {
        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()));
    }

    @Test
    abstract public void mainComponentDisplayedTest();

    protected void idIsDisplayed(int id) {
        onView(withId(id)).check(matches(isDisplayed()));
    }

}
