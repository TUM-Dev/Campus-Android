package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for EventDetails. Manages content that gets shown on the pagerView
 * TODO: combine this with KinoDetailsFragment
 */
public class EventDetailsFragment extends Fragment {

    private Context context;
    private Event event;
    private String url; // link to homepage
    private LayoutInflater inflater;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        context = root.getContext();

        event = EventsController.getEvents().get(position);
        showDetails(root);

        return rootView;
    }

    /**
     * creates the content of the fragment
     *
     * @param rootView view on which the content gets drawn
     */
    private void showDetails(LinearLayout rootView) {
        url = event.getLink();

        createEventHeader(rootView);
        createEventFooter(rootView);
    }

    private void addToRoot(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        rootView.addView(view);
    }

    private void addToRootWithPadding(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        // padding is done programmatically here because we need more padding at the end
        int padding = (int) context.getResources()
                                   .getDimension(R.dimen.padding_kino);
        int paddingRight = (int) context.getResources()
                                        .getDimension(R.dimen.padding_kino_right);
        int paddingEnd = (int) context.getResources()
                                      .getDimension(R.dimen.padding_kino_end);
        text.setPadding(padding, padding, paddingRight, paddingEnd);
        rootView.addView(view);
    }

    private void createEventFooter(LinearLayout root) {
        addToRoot(root, R.string.date, new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMANY).
                format(event.getDate()));
        addToRoot(root, R.string.location, event.getLocality());
        addToRootWithPadding(root, R.string.description, event.getDescription());
    }

    private void createEventHeader(LinearLayout rootView) {
        LinearLayout headerView = (LinearLayout) inflater.inflate(R.layout.event_header, rootView, false);

        // initialize all buttons
        Button link = headerView.findViewById(R.id.button_link);
        Button ticket = headerView.findViewById(R.id.button_ticket);
        Button export_calendar = headerView.findViewById(R.id.button_export_eventcalendar);
        ImageView cover = headerView.findViewById(R.id.kino_cover);
        ProgressBar progress = headerView.findViewById(R.id.kino_cover_progress);
        View error = headerView.findViewById(R.id.kino_cover_error);

        // onClickListeners
        link.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        // Export current activity to google calendar
        export_calendar.setOnClickListener(view -> addToCalendar());
        // Setup "Buy/Show ticket" button according to ticket status for current event
        if (EventsController.isEventBooked(event)) {
            ticket.setText(this.getString(R.string.show_ticket));
            ticket.setOnClickListener(view -> showTicket());
        } else {
            ticket.setText(this.getString(R.string.buy_ticket));
            ticket.setOnClickListener(view -> buyTicket());
        }

        // cover
        Picasso.get()
                .load(event.getImage())
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

    private void showTicket() {
        Intent intent = new Intent(context, ShowTicketActivity.class);
        intent.putExtra("eventID", event.getId());
        startActivity(intent);
    }

    private void buyTicket() {
        // TODO: message to server to create ticket
        Intent intent = new Intent(context, BuyTicketActivity.class);
        intent.putExtra("eventID", event.getId());
        startActivity(intent);
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getDate().getTime())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getDate().getTime() + 7200000)
                .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocality())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
