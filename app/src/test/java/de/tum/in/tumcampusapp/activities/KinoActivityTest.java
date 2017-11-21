package de.tum.in.tumcampusapp.activities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import de.tum.in.tumcampusapp.R;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class KinoActivityTest extends BaseActivityTest {

    @Test
    public void mainComponentDisplayedTest() {
        KinoActivity activity = Robolectric.setupActivity(KinoActivity.class);
        assertThat(activity.findViewById(R.id.no_movies_layout).isActivated()).isTrue();

        // TODO: download all from external, then check if movies are shown
    }
}
