package de.tum.in.tumcampusapp.component.ui.tufilm;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import de.tum.in.tumcampusapp.component.ui.tufilm.details.KinoDetailsFragment;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;

public class KinoAdapter extends FragmentStatePagerAdapter {

    private final List<Kino> movies;

    KinoAdapter(FragmentManager fm, List<Kino> kinos) {
        super(fm);
        movies = kinos;
    }

    @Override
    public Fragment getItem(int position) {
        String id = movies.get(position).getId();
        return KinoDetailsFragment.newInstance(id);
    }

    @Override
    public int getCount() {
        return movies.size();
    }

}
