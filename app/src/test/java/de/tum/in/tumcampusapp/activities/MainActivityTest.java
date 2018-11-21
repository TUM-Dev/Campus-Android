package de.tum.in.tumcampusapp.activities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.tum.in.tumcampusapp.TestApp;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class MainActivityTest extends BaseActivityTest {

    @Test
    @Override
    public void mainComponentDisplayedTest() {
        //idIsDisplayed(R.id.cards_view);

        //onView(withText(R.string.swipe_instruction)).check(matches(isDisplayed()));
    }
}
