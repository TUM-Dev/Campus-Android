package de.tum.in.tumcampus.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.RelativeLayout;

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
    private Cursor cursor;

    public KinoActivity(){
        super(Const.KINO, R.layout.activity_kino);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart(){
        super.onStart();

        km = new KinoManager(this);
        cursor = km.getAllFromDb();

        if (cursor.getCount() > 0){

            // set up ViewPager and adapter
            mpager = (ViewPager) findViewById(R.id.pager);
            kinoAdapter = new KinoAdapter(getSupportFragmentManager(), cursor);
            mpager.setAdapter(kinoAdapter);

        } else {
            // there are no movies in the semester holidays
            showCustomErrorLayout((RelativeLayout) findViewById(R.id.no_movies_layout));
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
    }

}

