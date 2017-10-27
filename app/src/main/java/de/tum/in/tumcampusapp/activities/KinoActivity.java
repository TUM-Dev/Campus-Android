package de.tum.in.tumcampusapp.activities;

import android.database.Cursor;
import android.support.v4.view.ViewPager;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.managers.KinoManager;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends BaseActivity {

    private Cursor cursor;

    public KinoActivity() {
        super(R.layout.activity_kino);
    }

    @Override
    protected void onStart() {
        super.onStart();

        KinoManager km = new KinoManager(this);
        cursor = km.getAllFromDb();

        // set up ViewPager and adapter
        ViewPager mpager = findViewById(R.id.pager);
        KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), cursor);
        mpager.setAdapter(kinoAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

}

