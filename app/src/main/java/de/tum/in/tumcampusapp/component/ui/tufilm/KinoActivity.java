package de.tum.in.tumcampusapp.component.ui.tufilm;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.news.KinoViewModel;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Activity to show TU Kino details (e.g. imdb rating)
 */
public class KinoActivity extends BaseActivity {
    private int startPosition;
    private ViewPager mPager;

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

        String date = getIntent().getStringExtra(Const.KINO_DATE);
        if (date != null) {
            startPosition = kinoViewModel.getPosition(date);
        } else {
            startPosition = 0;
        }

        Utils.log("startPos " + startPosition);

        // set up ViewPager and adapter
        mPager = findViewById(R.id.kinoViewPager);

        disposable.add(
                kinoViewModel.getAllKinos()
                        .subscribe(kinos -> {
                            FrameLayout errorLayout = findViewById(R.id.errors_layout);
                            LinearLayout noMoviesLayout = errorLayout.findViewById(R.id.no_movies_layout);

                            if (kinos.isEmpty()) {
                                errorLayout.setVisibility(View.VISIBLE);
                                noMoviesLayout.setVisibility(View.VISIBLE);
                            } else {
                                errorLayout.setVisibility(View.GONE);
                                noMoviesLayout.setVisibility(View.GONE);

                                KinoAdapter kinoAdapter =
                                        new KinoAdapter(getSupportFragmentManager(), kinos);
                                mPager.setAdapter(kinoAdapter);
                                mPager.setCurrentItem(startPosition);
                            }
                        }, throwable -> {
                            Utils.log(throwable);
                            setContentView(R.layout.layout_error);
                        } )
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable.dispose();
    }

}

