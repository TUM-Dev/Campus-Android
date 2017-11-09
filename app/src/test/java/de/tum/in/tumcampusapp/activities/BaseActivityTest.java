package de.tum.in.tumcampusapp.activities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public abstract class BaseActivityTest {

    @Test
    public void testDrawerLayout() {
       /* onView(withId(R.id.drawer_layout)).perform(open());
        onView(withId(R.id.left_drawer)).check(matches(isDisplayed()));
        onView(withId(R.id.drawer_layout)).perform(close());*/
    }

    @Test
    public void toolbarTest() {
      //  onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()));
    }

    @Test
    abstract public void mainComponentDisplayedTest();

}
