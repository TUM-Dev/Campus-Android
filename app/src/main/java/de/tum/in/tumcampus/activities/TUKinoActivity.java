package de.tum.in.tumcampus.activities;

import android.os.Bundle;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class TUKinoActivity extends ActivityForDownloadingExternal {

    public TUKinoActivity(){
        // TODO: create own layout
        super("kino", R.layout.activity_news);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
}
