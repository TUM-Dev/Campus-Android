package de.tum.in.tumcampusapp;

import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.close;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public abstract class BaseActivityTest {

    @Before
    public void setup() throws RemoteException {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).wakeUp();
    }

    @Test
    public void testDrawerLayout() throws InterruptedException {
        onView(withId(R.id.drawer_layout))
                .perform(open());

        onView(withId(R.id.left_drawer))
                .check(matches(isDisplayed()));

        onView(withId(R.id.drawer_layout))
                .perform(close());
    }

    @Test
    public void toolbarTest() {
        onView(withId(R.id.main_toolbar))
                .check(matches(isDisplayed()));
    }

    @Test
    abstract public void mainComponentDisplayedTest();

}
