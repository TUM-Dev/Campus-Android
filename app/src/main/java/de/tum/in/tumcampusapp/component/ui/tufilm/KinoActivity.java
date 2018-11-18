package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.os.Bundle;

import java.util.List;

import androidx.viewpager.widget.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ProgressActivity;
import de.tum.in.tumcampusapp.component.ui.news.KinoViewModel;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Activity to show TU Kino films
 */
public class KinoActivity extends ProgressActivity<Void> {

    private int startPosition;
    private ViewPager mPager;

    public KinoActivity() {
        super(R.layout.activity_kino);
    }

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.secondary_window_background);

        KinoLocalRepository.db = TcaDb.getInstance(this);

        KinoViewModel kinoViewModel = new KinoViewModel(
                KinoLocalRepository.INSTANCE, KinoRemoteRepository.INSTANCE, disposables);

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

        Disposable disposable = kinoViewModel
                .getAllKinos()
                .subscribe(this::showKinosOrPlaceholder, throwable -> {
                    Utils.log(throwable);
                    showError(R.string.error_something_wrong);
                });

        disposables.add(disposable);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

}

