package de.tum.in.tumcampus.activities;

import android.test.ActivityInstrumentationTestCase2;

import de.tum.in.tumcampus.R;

@SuppressWarnings("All")
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(de.tum.in.tumcampus.activities.MainActivity.class);
    }

    public void testMainActivityIsStarting() throws InterruptedException {
        assertEquals(getActivity().getText(R.string.app_name),
                getActivity().getSupportActionBar().getTitle());
    }

}
