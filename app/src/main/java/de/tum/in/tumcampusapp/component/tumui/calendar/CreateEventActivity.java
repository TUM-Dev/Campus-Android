package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequestFetchListener;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CreateEvent;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.DeleteEvent;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Allows the user to create (and edit) a private event in TUMonline.
 */
public class CreateEventActivity extends ActivityForAccessingTumOnline<CreateEvent> {
    private DateTime start;
    private DateTime end;
    private boolean editing;
    private TextView titleView;
    private TextView descriptionView;
    private TextView startDateView;
    private TextView startTimeView;
    private TextView endDateView;
    private TextView endTimeView;
    private Button createButton;
    private CalendarItem event;

    public CreateEventActivity() {
        super(TUMOnlineConst.Companion.getCREATE_EVENT(), R.layout.activity_create_event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editing = true;
            titleView.setText(extras.getString(Const.EVENT_TITLE));
            descriptionView.setText(extras.getString(Const.EVENT_COMMENT));
            createButton.setText(R.string.event_save_edit_button);
        }
        initStartEndDates(extras);
        setDateAndTimeListeners();
        createButton.setOnClickListener(view -> {
            if (end.isBefore(start)) {
                showErrorDialog(getString(R.string.create_event_time_error));
                return;
            }
            if (editing) {
                editEvent();
            } else {
                createEvent();
            }
        });
    }

    private void initViews() {
        titleView = findViewById(R.id.event_title);
        descriptionView = findViewById(R.id.event_description);
        startDateView = findViewById(R.id.event_start_date);
        startTimeView = findViewById(R.id.event_start_time);
        endDateView = findViewById(R.id.event_end_date);
        endTimeView = findViewById(R.id.event_end_time);
        createButton = findViewById(R.id.create_event_button);
    }

    private void initStartEndDates(Bundle extras) {
        if (editing) { // editing indicates extras are not null
            start = DateTimeUtils.INSTANCE.getDateTime(extras.getString(Const.EVENT_START));
            end = DateTimeUtils.INSTANCE.getDateTime(extras.getString(Const.EVENT_END));
        } else {
            // initial start: round up to the next full hour
            start = new DateTime()
                    .plusHours(1)
                    .withMinuteOfHour(0);

            // initial length: 1 hour
            end = new DateTime(start)
                    .plusHours(1);
        }

        updateDateViews();
        updateTimeViews();
    }

    private void setDateAndTimeListeners() {
        // DATE
        startDateView.setOnClickListener(view -> {
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                start = start.withDate(year, month, dayOfMonth);
                if (end.isBefore(start)) {
                    end = end.withDate(year, month, dayOfMonth);
                }
                updateDateViews();
            }, start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()).show();

        });
        endDateView.setOnClickListener(view -> {
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                end = end.withDate(year, month, dayOfMonth);
                updateDateViews();
            }, start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()).show();
        });

        // TIME
        startTimeView.setOnClickListener(view -> {
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                long eventLength = end.getMillis() - start.getMillis();
                start = start.withHourOfDay(hour)
                             .withMinuteOfHour(minute);
                end = end.withMillis(start.getMillis() + eventLength);
                updateTimeViews();
            }, start.getHourOfDay(), start.getMinuteOfDay(), true).show();
        });

        endTimeView.setOnClickListener(view -> {
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                end = end.withHourOfDay(hour)
                         .withMinuteOfHour(minute);
                updateTimeViews();
            }, end.getHourOfDay(), end.getMinuteOfDay(), true).show();
        });
    }

    private void updateTimeViews() {
        DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm")
                                                 .withLocale(Locale.getDefault());
        startTimeView.setText(format.print(start));
        endTimeView.setText(format.print(end));
    }

    private void updateDateViews() {
        DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy")
                                                 .withLocale(Locale.getDefault());
        startDateView.setText(format.print(start));
        endDateView.setText(format.print(end));
    }

    private void editEvent() {
        final String eventNr = getIntent().getExtras()
                                          .getString(Const.EVENT_NR);
        TUMOnlineRequest<DeleteEvent> request = new TUMOnlineRequest<>(
                TUMOnlineConst.Companion.getDELETE_EVENT(), this, true);
        request.setParameter(Const.EVENT_NR, eventNr);
        request.fetchInteractive(this, new TUMOnlineRequestFetchListener<DeleteEvent>() {
            @Override
            public void onNoInternetError() {
                showErrorDialog(getString(R.string.no_internet_connection));
            }

            @Override
            public void onFetch(DeleteEvent response) {
                Utils.log("Event successfully deleted (now creating the edited version)");
                TcaDb.getInstance(getApplicationContext())
                     .calendarDao()
                     .delete(eventNr);
                createEvent();
            }

            @Override
            public void onFetchCancelled() {
                showErrorDialog(getString(R.string.error_something_wrong));
            }

            @Override
            public void onFetchError(String errorReason) {
                showErrorDialog(errorReason);
            }

            @Override
            public void onNoDataToShow() {
                showErrorDialog(getString(R.string.error_something_wrong));
            }
        });
    }

    private void createEvent() {
        event = new CalendarItem();
        event.setDtstart(DateTimeUtils.INSTANCE.getDateTimeString(start));
        event.setDtend(DateTimeUtils.INSTANCE.getDateTimeString(end));

        String title = titleView.getText()
                                .toString();
        if (title.length() > 255) {
            title = title.substring(0, 255);
        }
        event.setTitle(title);

        String description = descriptionView.getText()
                                            .toString();
        if (description.length() > 4000) {
            description = description.substring(0, 4000);
        }
        event.setDescription(description);

        requestHandler.setParameter(Const.EVENT_TITLE, title);
        requestHandler.setParameter(Const.EVENT_COMMENT, description);
        requestHandler.setParameter(Const.EVENT_START, event.getDtstart());
        requestHandler.setParameter(Const.EVENT_END, event.getDtend());
        requestFetch();
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(R.string.error);
        builder.setIcon(R.drawable.ic_error_outline);
        builder.setNeutralButton(R.string.ok, null);
        builder.show();
    }

    @Override
    public void onFetch(CreateEvent response) {
        String nr = response.getEventNr();
        event.setNr(nr);
        TcaDb.getInstance(this)
             .calendarDao()
             .insert(event);
        finish();
    }

}
