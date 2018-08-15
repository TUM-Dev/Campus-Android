package de.tum.in.tumcampusapp.component.ui.ticket.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.tumui.calendar.CreateEventActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.BuyTicketActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Fragment for EventDetails. Manages content that gets shown on the pagerView
 */
public class EventDetailsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private int eventId;
    private Event event;

    private TextView remainingTicketsTextView;
    private SwipeRefreshLayout mSwipeLayout;

    private EventsController eventsController;
    private final Disposable disposable = new CompositeDisposable();

    public static Fragment newInstance(int eventId) {
        Fragment fragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putInt("eventID", eventId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getArguments() != null) {
            eventId = getArguments().getInt("eventID", -1);

            if (eventId != -1) {
                eventsController = new EventsController(getContext());
                event = eventsController.getEventById(eventId);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(container.getContext())
            .inflate(R.layout.fragment_event_details, container, false);

        mSwipeLayout = view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);

        showEventDetails(view);
        return view;
    }

    @Override
    public void onRefresh() {
        loadAvailableTicketCount();
    }

    /**
     * creates the content of the fragment
     *
     * @param view view on which the content gets drawn
     */
    private void showEventDetails(View view) {
        ImageView coverImageView = view.findViewById(R.id.image_view);
        ProgressBar progressBar = view.findViewById(R.id.image_progress_bar);

        Context context = view.getContext();

        if (event.getImageUrl() != null) {
            Picasso.get()
                    .load(event.getImageUrl())
                    .into(coverImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
        }

        AppCompatButton ticketButton = view.findViewById(R.id.ticket_button);
        if (eventsController.isEventBooked(event)) {
            ticketButton.setText(getString(R.string.show_ticket));
            ticketButton.setOnClickListener(v -> showTicket());
        } else {
            ticketButton.setText(getString(R.string.buy_ticket));
            ticketButton.setOnClickListener(v -> buyTicket());
        }

        remainingTicketsTextView = view.findViewById(R.id.remaining_tickets_text_view);

        TextView dateTextView = view.findViewById(R.id.date_text_view);
        dateTextView.setText(event.getFormattedStartDateTime(context));
        dateTextView.setOnClickListener(v -> new AddToCalendarDialog(context).show());

        TextView locationTextView = view.findViewById(R.id.location_text_view);
        locationTextView.setText(event.getLocality());
        locationTextView.setOnClickListener(this::showMap);

        TextView descriptionTextView = view.findViewById(R.id.description_text_view);
        descriptionTextView.setText(event.getDescription());

        AppCompatButton linkButton = view.findViewById(R.id.link_button);
        if (event.getEventUrl().isEmpty()) {
            linkButton.setVisibility(View.GONE);
        } else {
            linkButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getEventUrl()));
                startActivity(intent);
            });

        }

        loadAvailableTicketCount();
    }

    private void loadAvailableTicketCount() {
        TUMCabeClient
                .getInstance(getContext())
                .fetchTicketStats(eventId, new retrofit2.Callback<List<TicketStatus>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<TicketStatus>> call,
                                           @NonNull Response<List<TicketStatus>> response) {
                        // Statuses is array of TicketStats, each containing info about one ticket
                        // type associated with the event.
                        List<TicketStatus> statuses = response.body();
                        if (statuses != null) {
                            int sum = 0;
                            for (TicketStatus status : statuses) {
                                sum += status.getAvailableTicketCount();
                            }

                            String text = String.format(Locale.getDefault(), "%d", sum);
                            remainingTicketsTextView.setText(text);
                        } else {
                            remainingTicketsTextView.setText(R.string.unknown);
                        }

                        mSwipeLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<TicketStatus>> call,
                                          @NonNull Throwable t) {
                        Utils.log(t);
                        remainingTicketsTextView.setText(R.string.unknown);
                        mSwipeLayout.setRefreshing(false);
                    }
                });
    }

    private void showTicket() {
        Intent intent = new Intent(getContext(), ShowTicketActivity.class);
        intent.putExtra("eventID", event.getId());
        startActivity(intent);
    }

    private void buyTicket() {
        Intent intent = new Intent(getContext(), BuyTicketActivity.class);
        intent.putExtra("eventID", event.getId());
        startActivity(intent);
    }

    private void addToTUMCalendar() {
        DateTime eventEndDateTime = event.getEndTime() != null
                ? event.getEndTime() : event.getStartTime().plus(Event.defaultDuration);

        String eventEnd = DateTimeUtils.INSTANCE.getDateTimeString(eventEndDateTime);

        Intent intent = new Intent(getContext(), CreateEventActivity.class)
                .putExtra(Const.EVENT_EDIT, false)
                .putExtra(Const.EVENT_TITLE, event.getTitle())
                .putExtra(Const.EVENT_COMMENT, event.getDescription())
                .putExtra(Const.EVENT_START, DateTimeUtils.INSTANCE.getDateTimeString(event.getStartTime()))
                .putExtra(Const.EVENT_END, eventEnd);

        startActivity(intent);
    }

    private void addToExternalCalendar() {
        DateTime eventEndDateTime = event.getEndTime() != null
                ? event.getEndTime() : event.getStartTime().plus(Event.defaultDuration);

        String eventEnd = DateTimeUtils.INSTANCE.getDateTimeString(eventEndDateTime);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartTime().getMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventEnd)
                .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocality())
                //Indicates that this event is free time and will not conflict with other events
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        startActivity(intent);
    }

    private void showMap(View view) {
        TextView textView = (TextView) view;
        String url = "http://maps.google.co.in/maps?q=" + textView.getText();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        disposable.dispose();
    }

    private class AddToCalendarDialog extends Dialog {

        private AddToCalendarDialog(Context context) {
            super(context);
            this.setContentView(R.layout.dialog_add_to_calendar);

            Button cancelButton = this.findViewById(R.id.add_to_calendar_cancel_button);
            Button externalCalendarButton = this.findViewById(R.id.add_to_external_calendar_button);
            Button tumCalendarButton = this.findViewById(R.id.add_to_tum_calendar_button);

            cancelButton.setOnClickListener(view -> AddToCalendarDialog.this.dismiss());

            externalCalendarButton.setOnClickListener(view -> {
                addToExternalCalendar();
                AddToCalendarDialog.this.dismiss();
            });

            tumCalendarButton.setOnClickListener(view -> {
                addToTUMCalendar();
                AddToCalendarDialog.this.dismiss();
            });
        }

    }

}
