package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment {

    private View rootView;

    private KinoViewModel kinoViewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public static KinoDetailsFragment newInstance(int position) {
        KinoDetailsFragment fragment = new KinoDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(Const.POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        KinoLocalRepository.db = TcaDb.getInstance(context);
        kinoViewModel = new KinoViewModel(
                KinoLocalRepository.INSTANCE, KinoRemoteRepository.INSTANCE, disposables);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int position = getArguments().getInt(Const.POSITION);
        Disposable disposable = kinoViewModel
                .getKinoByPosition(position)
                .subscribe(this::showMovieDetails);
        disposables.add(disposable);
    }

    private void showMovieDetails(Kino kino) {
        loadPoster(kino);

        TextView dateTextView = rootView.findViewById(R.id.dateTextView);
        dateTextView.setText(kino.getFormattedDate());

        TextView runtimeTextView = rootView.findViewById(R.id.runtimeTextView);
        runtimeTextView.setText(kino.getRuntime());

        TextView ratingTextView = rootView.findViewById(R.id.ratingTextView);
        ratingTextView.setText(kino.getRating());

        int colorPrimary = ContextCompat.getColor(requireContext(), R.color.color_primary);
        setCompoundDrawablesTint(dateTextView, colorPrimary);
        setCompoundDrawablesTint(runtimeTextView, colorPrimary);
        setCompoundDrawablesTint(ratingTextView, colorPrimary);

        TextView descriptionTextView = rootView.findViewById(R.id.descriptionTextView);
        descriptionTextView.setText(kino.getFormattedDescription());

        TextView genresTextView = rootView.findViewById(R.id.genresTextView);
        genresTextView.setText(kino.getGenre());

        TextView releaseYearTextView = rootView.findViewById(R.id.releaseYearTextView);
        releaseYearTextView.setText(kino.getYear());

        TextView actorsTextView = rootView.findViewById(R.id.actorsTextView);
        actorsTextView.setText(kino.getActors());

        TextView directorTextView = rootView.findViewById(R.id.directorTextView);
        directorTextView.setText(kino.getDirector());

        MaterialButton moreInfoButton = rootView.findViewById(R.id.moreInfoButton);
        moreInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kino.getLink()));
            startActivity(intent);
        });
    }

    private void loadPoster(Kino kino) {
        MaterialButton trailerButton = rootView.findViewById(R.id.trailerButton);
        trailerButton.setOnClickListener(v -> showTrailer(kino));

        ImageView posterView = rootView.findViewById(R.id.kino_cover);
        ProgressBar progressBar = rootView.findViewById(R.id.kino_cover_progress);

        Picasso.get()
                .load(kino.getCover())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        progressBar.setVisibility(View.GONE);
                        posterView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // Free ad space
                    }
                });
    }

    private void setCompoundDrawablesTint(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void showTrailer(Kino kino) {
        String url = kino.getTrailerSearchUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        requireActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

}
