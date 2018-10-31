package de.tum.in.tumcampusapp.component.ui.tufilm;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.news.KinoDetailsFragment;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;

public class KinoAdapter extends FragmentStatePagerAdapter {

    private final List<Kino> movies;

    KinoAdapter(FragmentManager fm, List<Kino> kinos) {
        super(fm);
        movies = kinos;
    }

    @Override
    public Fragment getItem(int position) {
        return KinoDetailsFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return movies.size();
    }

}
