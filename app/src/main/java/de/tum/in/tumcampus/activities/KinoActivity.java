package de.tum.in.tumcampus.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.KinoAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.managers.KinoManager;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends ActivityForDownloadingExternal {

    private KinoManager km;
    private ViewPager mpager;
    private KinoAdapter kinoAdapter;

    public KinoActivity(){
        super(Const.KINO, R.layout.activity_kino);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestDownload(false);

        mpager = (ViewPager) findViewById(R.id.pager);
        kinoAdapter = new KinoAdapter(getSupportFragmentManager());
        mpager.setAdapter(kinoAdapter);
    }

    @Override
    protected void onStart(){
        super.onStart();

        km = new KinoManager(this);
        Cursor cursor = km.getAllFromDb(this);

    }

}

