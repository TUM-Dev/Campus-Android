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
import android.widget.Toast;

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
import de.tum.in.tumcampusapp.component.ui.ticket.EventHelper;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity;
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

import static de.tum.in.tumcampusapp.utils.Const.KEY_EVENT_ID;

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment {

    private View rootView;
    private Event event;
    private EventsController eventsController;

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
    public void onResume() {
        super.onResume();
        if (event != null) {
            initBuyOrShowTicket(event);
        }
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
        this.event = event;
        this.eventsController = new EventsController(getContext());
        initBuyOrShowTicket(event);

        rootView.findViewById(R.id.eventInformation).setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.locationTextView)).setText(event.getLocality());
        loadAvailableTicketCount(event);
    }

    private void initBuyOrShowTicket(Event event) {
        MaterialButton ticketButton = rootView.findViewById(R.id.buyTicketButton);
        if (eventsController.isEventBooked(event)) {
            ticketButton.setText(R.string.show_ticket);
            ticketButton.setVisibility(View.VISIBLE);
            ticketButton.setOnClickListener(view -> {
                if (event == null) {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), ShowTicketActivity.class);
                intent.putExtra(KEY_EVENT_ID, event.getId());
                startActivity(intent);
            });
        } else if (!EventHelper.Companion.isEventImminent(event)) {
            ticketButton.setText(R.string.buy_ticket);
            ticketButton.setVisibility(View.VISIBLE);
            ticketButton.setOnClickListener(
                    view -> EventHelper.Companion.buyTicket(this.event, ticketButton, getContext()));
        }
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
        Disposable disposable = kinoViewModel
                .getEventByMovieId(kino.getId())
                .subscribe(this::showEventTicketDetails);
        disposables.add(disposable);

        loadPoster(kino);

        TextView dateTextView = rootView.findViewById(R.id.dateTextView);
        dateTextView.setText(kino.getFormattedShortDate());

        TextView runtimeTextView = rootView.findViewById(R.id.runtimeTextView);
        runtimeTextView.setText(kino.getRuntime());

        TextView ratingTextView = rootView.findViewById(R.id.ratingTextView);
        ratingTextView.setText(kino.getFormattedRating());

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
