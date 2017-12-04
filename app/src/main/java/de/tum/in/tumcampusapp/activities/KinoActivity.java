package de.tum.in.tumcampusapp.activities;

import android.support.v4.view.ViewPager;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dataAccessObjects.KinoDao;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends BaseActivity {

    public KinoActivity() {
        super(R.layout.activity_kino);
    }

    @Override
    protected void onStart() {
        super.onStart();

        KinoDao dao = TcaDb.getInstance(this)
                           .kinoDao();
        // set up ViewPager and adapter
        ViewPager mpager = findViewById(R.id.pager);
        KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), dao.getAll());
        mpager.setAdapter(kinoAdapter);
    }
}

