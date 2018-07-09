package de.tum.in.tumcampusapp.component.ui.ticket;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CreateEventActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for EventDetails. Manages content that gets shown on the pagerView
 */
public class EventDetailsFragment extends Fragment {

    private Context context;
    private Event event;
    private TextView eventLocationTextView;
    private String url; // link to homepage
    private LayoutInflater inflater;
    private EventsController eventsController;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        eventsController = new EventsController(this.getContext());

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        context = root.getContext();

        event = eventsController.getEvents().get(position);

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


    private void createEventHeader(LinearLayout rootView) {
        LinearLayout headerView = (LinearLayout) inflater.inflate(R.layout.event_header, rootView, false);

        // initialize all buttons
        Button ticket = headerView.findViewById(R.id.button_ticket);
        ImageView cover = headerView.findViewById(R.id.kino_cover);
        ProgressBar progress = headerView.findViewById(R.id.kino_cover_progress);
        View error = headerView.findViewById(R.id.kino_cover_error);

        // onClickListeners
        // Setup "Buy/Show ticket" button according to ticket status for current event
        if (eventsController.isEventBooked(event)) {
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


    private void createEventFooter(LinearLayout rootView) {
        View footerView = inflater.inflate(R.layout.event_footer, rootView, false);
        // initialize all TextView
        TextView eventDateTextView = footerView.findViewById(R.id.event_date);
        eventLocationTextView = footerView.findViewById(R.id.event_location);
        TextView eventRemainingTicketTextView = footerView.findViewById(R.id.event_remainingticket);
        TextView eventDescriptionTextView = footerView.findViewById(R.id.event_description);
        TextView eventLinkTextView = footerView.findViewById(R.id.event_link);

        eventDateTextView.setText(DateTimeUtils.INSTANCE.getDateTimeString(event.getStart()));

        // open "add to calendar" dialog on click
        eventDateTextView.setOnClickListener(v -> new AddToCalendarDialog(context).show());

        //set Location link
        String eventLocationString = event.getLocality();
        eventLocationTextView.setText(eventLocationString);
        eventLocationTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        eventLocationTextView.setOnClickListener(view -> showMap());

        //set remaining tickets,following code is just for testing purpose.
        //TODO:The remaining tickets should get from backend.Like event.getremainningtickets()
        int remainingTickets = 30;
        String remainingTicketsString = remainingTickets + " Tickets left!";
        eventRemainingTicketTextView.setText(remainingTicketsString);

        String eventDescriptionString = event.getDescription();
        eventDescriptionTextView.setText(eventDescriptionString);

        eventLinkTextView.setText(this.getString(R.string.link));
        eventLinkTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        eventLinkTextView.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        int padding = (int) context.getResources()
                .getDimension(R.dimen.padding_kino);
        int paddingRight = (int) context.getResources()
                .getDimension(R.dimen.padding_kino_right);
        int paddingEnd = (int) context.getResources()
                .getDimension(R.dimen.padding_kino_end);
        eventLinkTextView.setPadding(padding, padding, paddingRight, paddingEnd);
        rootView.addView(footerView);
    }

    private void showTicket() {
        Intent intent = new Intent(context, ShowTicketActivity.class);
        intent.putExtra("eventID", event.getId());
        startActivity(intent);
    }

    private void buyTicket() {
        Intent intent = new Intent(context, BuyTicketActivity.class);
        intent.putExtra("eventID", event.getId());
        startActivity(intent);
    }

    private void addToTUMCalendar() {
        Intent intent = new Intent(context, CreateEventActivity.class);
        intent.putExtra(Const.EVENT_EDIT, false);
        intent.putExtra(Const.EVENT_TITLE, event.getTitle());
        intent.putExtra(Const.EVENT_COMMENT, event.getDescription());
        intent.putExtra(Const.EVENT_START, DateTimeUtils.INSTANCE.getDateTimeString(event.getStart()));
        intent.putExtra(Const.EVENT_END, DateTimeUtils.INSTANCE.getDateTimeString(event.getEnd() != null
                ? event.getEnd()
                : event.getStart().plus(Const.EVENT_DEFAULT_DURATION)));
        startActivity(intent);
    }

    private void addToExternalCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStart().getMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEnd() != null
                        ? event.getEnd()
                        : event.getStart().plus(Const.EVENT_DEFAULT_DURATION).getMillis())
                .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocality())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);//Indicates that this event is free time and will not conflict with other events.
        startActivity(intent);
    }

    private void showMap() {
        eventLocationTextView.setTextColor(Color.RED);
        String map = "http://maps.google.co.in/maps?q=" + eventLocationTextView.getText();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(mapIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
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
