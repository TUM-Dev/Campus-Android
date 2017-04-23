package de.tum.in.tumcampusapp.activities;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.KinoAdapter;
import de.tum.in.tumcampusapp.entities.Movie;
import de.tum.in.tumcampusapp.managers.KinoManager;

/**
 * Activity to show TU Movie details (e.g. imdb rating)
 */
public class KinoActivity extends BaseActivity {

    public KinoActivity() {
        super(R.layout.activity_kino);
    }

    @Override
    protected void onStart() {
        super.onStart();

        KinoManager km = new KinoManager(this);
        List<Movie> allMovies = km.getAll();

        if (allMovies.size() > 0) {

            // set up ViewPager and adapter
            ViewPager mpager = (ViewPager) findViewById(R.id.pager);
            KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), allMovies);
            mpager.setAdapter(kinoAdapter);

        } else {
            // there are no movies in the semester holidays
            RelativeLayout rl =((RelativeLayout) findViewById(R.id.no_movies_layout));
            rl.setVisibility(View.VISIBLE);
        }

    }

}

