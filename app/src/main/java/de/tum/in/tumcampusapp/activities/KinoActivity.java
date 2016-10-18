package de.tum.in.tumcampusapp.activities;

import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.widget.RelativeLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.managers.KinoManager;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends ActivityForDownloadingExternal {

    private Cursor cursor;

    public KinoActivity() {
        super(Const.KINO, R.layout.activity_kino);
    }

    @Override
    protected void onStart() {
        super.onStart();

        KinoManager km = new KinoManager(this);
        cursor = km.getAllFromDb();

        if (cursor.getCount() > 0) {

            // set up ViewPager and adapter
            ViewPager mpager = (ViewPager) findViewById(R.id.pager);
            KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), cursor);
            mpager.setAdapter(kinoAdapter);

        } else {
            // there are no movies in the semester holidays
            showCustomErrorLayout((RelativeLayout) findViewById(R.id.no_movies_layout));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

}

