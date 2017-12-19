package de.tum.in.tumcampusapp.activities;

import android.support.v4.view.ViewPager;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dataAccessObjects.KinoDao;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

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
        List<Kino> movies = dao.getAll();

        if (movies.isEmpty()) {
            setContentView(R.layout.layout_no_movies);
        } else {
            ViewPager mpager = findViewById(R.id.pager);
            KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), movies);
            mpager.setAdapter(kinoAdapter);
        }
    }
}

