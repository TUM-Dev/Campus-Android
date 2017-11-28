package de.tum.in.tumcampusapp.activities;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.runner.RunWith;

import de.tum.in.tumcampusapp.R;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CurriculaActivityTest extends BaseActivityTest {

    @Rule
    public ActivityTestRule<CurriculaActivity> mActivityRule = new ActivityTestRule<>(CurriculaActivity.class);

    @Override
    public void mainComponentDisplayedTest() {
        idIsDisplayed(R.id.activity_curricula_list_view);
    }
}
