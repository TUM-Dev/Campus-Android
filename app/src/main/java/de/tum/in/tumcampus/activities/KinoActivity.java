package de.tum.in.tumcampus.activities;

import android.os.Bundle;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.auxiliary.Const;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends ActivityForDownloadingExternal {

    public KinoActivity(){
        super(Const.KINO, R.layout.activity_kino);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestDownload(false);
    }

    @Override
    protected void onStart(){
        super.onStart();



    }
}
