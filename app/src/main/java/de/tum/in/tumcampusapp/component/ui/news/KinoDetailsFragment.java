package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private void showEventTicketDetails(Event event) {
        rootView.findViewById(R.id.eventInformation).setVisibility(View.VISIBLE);
        MaterialButton buyButton = rootView.findViewById(R.id.buyTicketButton);
        buyButton.setVisibility(View.VISIBLE);
        // TODO(bronger) add onClickListener

        ((TextView) rootView.findViewById(R.id.locationTextView)).setText(event.getLocality());
        loadAvailableTicketCount(event);
    }

    private void loadAvailableTicketCount(Event event) {
        TUMCabeClient.getInstance(getContext())
                .fetchTicketStats(event.getId(), new Callback<List<TicketStatus>>() {
                    @Override
                    public void onResponse(Call<List<TicketStatus>> call, Response<List<TicketStatus>> response) {
                        List<TicketStatus> statusList = response.body();
                        if (statusList == null) {
                            if (!isDetached()) {
                                ((TextView) rootView.findViewById(R.id.remainingTicketsTextView))
                                        .setText(R.string.unknown);
                            }
                        }
                        int sum = 0;
                        for (TicketStatus status : statusList) {
                            sum += status.getAvailableTicketCount();
                        }
                        String text = String.format(Locale.getDefault(), "%d", sum);
                        if (!isDetached()) {
                            ((TextView) rootView.findViewById(R.id.remainingTicketsTextView))
                                    .setText(text);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TicketStatus>> call, Throwable t) {
                        if (!isDetached()) {
                            ((TextView) rootView.findViewById(R.id.remainingTicketsTextView))
                                    .setText(R.string.unknown);
                        }
                    }
                });
    }

    private void showMovieDetails(Kino kino) {
        // TODO(bronger) replace kino.link with kino.id as soon as event has a reference to the movie
        String link = kino.getLink().replace("www.", "");
        Disposable disposable = kinoViewModel
                .getEventByMovieId(link)
                .subscribe(this::showEventTicketDetails);
        disposables.add(disposable);

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
