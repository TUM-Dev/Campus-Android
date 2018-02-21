package de.tum.in.tumcampusapp.component.news.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.news.adapter.KinoAdapter;
import de.tum.in.tumcampusapp.database.TcaDb;

import de.tum.in.tumcampusapp.component.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.news.viewmodel.KinoViewModel;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends BaseActivity {

    public KinoActivity() {
        super(R.layout.activity_kino);
    }

    private KinoViewModel kinoViewModel;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KinoLocalRepository.db = TcaDb.getInstance(this);
        kinoViewModel = new KinoViewModel(KinoLocalRepository.INSTANCE, KinoRemoteRepository.INSTANCE, disposable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        View noMovies = findViewById(R.id.no_movies_layout);
        // set up ViewPager and adapter
        ViewPager mpager = findViewById(R.id.pager);
        kinoViewModel.getAllKinos()
                     .doOnError(throwable -> setContentView(R.layout.layout_error))
                     .subscribe(kinos -> {
                         if (kinos.isEmpty()) {
                             noMovies.setVisibility(View.VISIBLE);
                         } else {
                             noMovies.setVisibility(View.GONE);
                             KinoAdapter kinoAdapter = new KinoAdapter(getSupportFragmentManager(), kinos);
                             mpager.setAdapter(kinoAdapter);
                         }
                     });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}

