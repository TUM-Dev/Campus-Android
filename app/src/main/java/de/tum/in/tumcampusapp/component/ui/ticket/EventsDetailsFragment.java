package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for EventDetails. Manages content that gets shown on the pagerView
 */
public class EventsDetailsFragment extends Fragment {

    private Context context;
    private Event event;
    private String url; // link to homepage
    private LayoutInflater inflater;

    private boolean isBooked = true;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_eventdetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        EvnentLocalRepository.db = TcaDb.getInstance(context);
        EventViewModel eventViewModel = new EventViewModel(EventLocalRepository.INSTANCE, EventRemoteRepository.INSTANCE, disposable);
        context = root.getContext();

        // TODO: set isBooked if the user has already bought a ticket

        eventViewModel.getEventByPosition(position)
                .subscribe(event1 -> {
                    event = event1;
                    showDetails(root);
                });

        return rootView;
    }

    /**
     * creates the content of the fragment
     *
     * @param rootView view on which the content gets drawn
     */
    private void showDetails(LinearLayout rootView) {
        url = event.getLink();

        createeventHeader(rootView);
        createeventFooter(rootView);
    }

    private void addToRoot(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.event_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        rootView.addView(view);
    }

    private void addToRootWithPadding(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.event_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        // padding is done programmatically here because we need more padding at the end
        int padding = (int) context.getResources()
                .getDimension(R.dimen.padding_event);
        int paddingRight = (int) context.getResources()
                .getDimension(R.dimen.padding_event_right);
        int paddingEnd = (int) context.getResources()
                .getDimension(R.dimen.padding_event_end);
        text.setPadding(padding, padding, paddingRight, paddingEnd);
        rootView.addView(view);
    }

    private void createEventFooter(LinearLayout root) {
        addToRoot(root, R.string.genre, event.getGenre());
        addToRoot(root, R.string.director, event.getDirector());
        addToRoot(root, R.string.actors, event.getActors());
        addToRootWithPadding(root, R.string.description, event.getDescription());
    }

    private void createEventHeader(LinearLayout rootView) {
        LinearLayout headerView = (LinearLayout) inflater.inflate(R.layout.event_header, rootView, false);

        // initialize all buttons
        Button date = headerView.findViewById(R.id.button_date);
        Button link = headerView.findViewById(R.id.button_link);
        Button imdb = headerView.findViewById(R.id.button_imdb);
        Button year = headerView.findViewById(R.id.button_year);
        Button runtime = headerView.findViewById(R.id.button_runtime);
        Button trailer = headerView.findViewById(R.id.button_trailer);
        Button ticket = headerView.findViewById(R.id.button_ticket);
        ImageView cover = headerView.findViewById(R.id.event_cover);
        ProgressBar progress = headerView.findViewById(R.id.event_cover_progress);
        View error = headerView.findViewById(R.id.event_cover_error);

        // set text for buttons
        date.setText(EventDetailsFragment.formDateString(DateUtils.getDateString(event.getDate())));
        imdb.setText(event.getRating() + " / 10");
        year.setText(event.getYear());
        runtime.setText(event.getRuntime());

        // onClickListeners
        link.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        year.setOnClickListener(view -> Toast.makeText(context, R.string.year, Toast.LENGTH_SHORT).show());
        trailer.setOnClickListener(view -> showTrailer());

        // Setup "Buy/Show ticket" button according to ticket status for current event
        if (isBooked) {
            ticket.setText("Show ticket");
            ticket.setOnClickListener(view -> showTicket());
        } else {
            ticket.setText("Buy ticket");
            ticket.setOnClickListener(view -> buyTicket());
        }

        // cover
        Picasso.get()
                .load(event.getCover())
                .into(cover, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress.setVisibility(View.GONE);
                        error.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progress.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });

        rootView.addView(headerView);
    }

    //open ShowTicketActivity activity, and transfer current movie data to it.
    //TODO: The String data is just a example, it should be get from correspongding movie which now show on the screen.
    //Time and place of movie  is fixed. Only the movie title and date should be transfered to ShowTicketActivity activity
    //somethings in event class,object
    private void showTicket() {
        String data = "KingsMan 08.05 " + "\n" + "Filmbegin: 20:00 o'clock " + "\n" + " 1. Stock, Hörsaal 1200 (Carl-von-Linde-Hörsaal) Arcisstraße 21";
        Intent intent = new Intent(getActivity().getApplicationContext(), ShowTicketActivity.class);
        intent.putExtra("movie_data", data);
        startActivity(intent);
    }

    private void buyTicket() {
        // TODO: go to payment activity (to be implemented)
    }

    public void showTrailer() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=" + getTrailerSearchString()));
        try {
            context.startActivity(webIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, getString(R.string.show_trailer_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String getTrailerSearchString() {
        String search = event.getTitle();
        search = search.split(": ")[1];
        search = "trailer " + search;
        if (!search.contains("OV")) {
            search += " german deutsch";
        }
        search = search.replace(' ', '+');

        return search;
    }

    /**
     * formats the dateString
     *
     * @param date Date string stored in database
     * @return formated dateString
     */
    private static CharSequence formDateString(String date) {
        return date.substring(8, 10) + '.' + date.substring(5, 7) + '.';
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
