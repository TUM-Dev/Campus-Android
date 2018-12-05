package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ProgressActivity;
import de.tum.in.tumcampusapp.component.ui.tufilm.di.KinoModule;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.di.ViewModelFactory;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * Activity to show TU Kino films
 */
public class KinoActivity extends ProgressActivity<Void> {

    private int startPosition;
    private ViewPager mPager;

    @Inject
    Provider<KinoViewModel> viewModelProvider;

    public KinoActivity() {
        super(R.layout.activity_kino);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().kinoComponent()
                .kinoModule(new KinoModule())
                .build()
                .inject(this);

        getWindow().setBackgroundDrawableResource(R.color.secondary_window_background);

        ViewModelFactory<KinoViewModel> factory = new ViewModelFactory<>(viewModelProvider);
        KinoViewModel kinoViewModel = ViewModelProviders.of(this, factory).get(KinoViewModel.class);

        String movieDate = getIntent().getStringExtra(Const.KINO_DATE);
        int movieId = getIntent().getIntExtra(Const.KINO_ID, -1);

        if (movieDate != null) {
            startPosition = kinoViewModel.getPositionByDate(movieDate);
        } else if (movieId != -1) {
            startPosition = kinoViewModel.getPositionById("" + movieId);
        } else {
            startPosition = 0;
        }
        mPager = findViewById(R.id.pager);

        int margin = getResources().getDimensionPixelSize(R.dimen.material_default_padding);
        mPager.setPageMargin(margin);

        kinoViewModel.getKinos().observe(this, this::showKinosOrPlaceholder);
        kinoViewModel.getError().observe(this, this::showError);
    }

    private void showKinosOrPlaceholder(List<Kino> kinos) {
        if (kinos.isEmpty()) {
            showEmptyResponseLayout(R.string.no_movies, R.drawable.no_movies);
            return;
        }

        KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), kinos);
        mPager.setAdapter(kinoAdapter);
        mPager.setCurrentItem(startPosition);
    }

}

