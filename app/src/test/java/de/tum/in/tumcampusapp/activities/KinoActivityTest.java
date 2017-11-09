package de.tum.in.tumcampusapp.activities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import de.tum.in.tumcampusapp.R;

import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
public class KinoActivityTest extends BaseActivityTest {

    @Test
    public void mainComponentDisplayedTest() {
        KinoActivity activity = Robolectric.setupActivity(KinoActivity.class);
        assertFalse(activity.findViewById(R.id.no_movies_layout).isActivated());// TODO: should be true

        // TODO: download all from external, then check if movies are shown
    }
}
