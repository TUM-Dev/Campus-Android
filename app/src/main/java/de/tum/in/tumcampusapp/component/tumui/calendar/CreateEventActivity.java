package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Allows the user to create (and edit) a private event in TUMonline.
 */
public class CreateEventActivity extends ActivityForAccessingTumOnline<CreateEvent> {
    private Calendar start, end;
    private boolean editing;
    private TextView titleView, descriptionView, startDateView, startTimeView, endDateView, endTimeView;
    private Button createButton;
    private CalendarItem event;

    public CreateEventActivity(){
        super(TUMOnlineConst.Companion.getCREATE_EVENT(), R.layout.activity_create_event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            editing = true;
            titleView.setText(extras.getString(Const.EVENT_TITLE));
            descriptionView.setText(extras.getString(Const.EVENT_COMMENT));
            createButton.setText(R.string.event_save_edit_button);
        }
        initStartEndDates(extras);
        setDateAndTimeListeners();
        createButton.setOnClickListener(view -> {
            if (end.before(start)){
                showErrorDialog(getString(R.string.create_event_time_error));
                return;
            }
            if (editing){
                editEvent();
            } else {
                createEvent();
            }
        });
    }

    private void initViews(){
        titleView = findViewById(R.id.event_title);
        descriptionView = findViewById(R.id.event_description);
        startDateView = findViewById(R.id.event_start_date);
        startTimeView = findViewById(R.id.event_start_time);
        endDateView = findViewById(R.id.event_end_date);
        endTimeView = findViewById(R.id.event_end_time);
        createButton = findViewById(R.id.create_event_button);
    }

    private void initStartEndDates(Bundle extras){
        start = Calendar.getInstance();
        end = Calendar.getInstance();

        if (editing){
            start.setTime(DateUtils.getDateTime(extras.getString(Const.EVENT_START)));
            end.setTime(DateUtils.getDateTime(extras.getString(Const.EVENT_END)));
        } else {
            // initial start: round up to the next full hour
            start.add(Calendar.HOUR_OF_DAY, 1);
            start.set(Calendar.MINUTE, 0);

            // initial length: 1 hour
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.HOUR_OF_DAY, 1);
        }

        updateDateViews();
        updateTimeViews();
    }

    private void setDateAndTimeListeners(){

        // DATE
        startDateView.setOnClickListener(view -> {
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                start.set(year, month, dayOfMonth);
                if (end.before(start)){
                    end.set(year, month, dayOfMonth);
                }
                updateDateViews();
            }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH)).show();

        });
        endDateView.setOnClickListener(view -> {
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                end.set(year, month, dayOfMonth);
                updateDateViews();
            }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH)).show();
        });

        // TIME
        startTimeView.setOnClickListener(view -> {
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                long eventLength = end.getTimeInMillis() - start.getTimeInMillis();
                start.set(Calendar.HOUR_OF_DAY, hour);
                start.set(Calendar.MINUTE, minute);
                end.setTimeInMillis(start.getTimeInMillis() + eventLength);
                updateTimeViews();
            }, start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), true).show();
        });

        endTimeView.setOnClickListener(view -> {
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                end.set(Calendar.HOUR_OF_DAY, hour);
                end.set(Calendar.MINUTE, minute);
                updateTimeViews();
            }, end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE), true).show();
        });
    }

    private void updateTimeViews(){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        startTimeView.setText(format.format(start.getTime()));
        endTimeView.setText(format.format(end.getTime()));
    }
    private void updateDateViews(){
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        startDateView.setText(format.format(start.getTime()));
        endDateView.setText(format.format(end.getTime()));
    }

    private void editEvent(){
        final String eventNr = getIntent().getExtras().getString(Const.EVENT_NR);
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
                TcaDb.getInstance(getApplicationContext()).calendarDao().delete(eventNr);
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

    private void createEvent(){
        event = new CalendarItem();
        event.setDtstart(DateUtils.getDateTimeString(start.getTime()));
        event.setDtend(DateUtils.getDateTimeString(end.getTime()));

        String title = titleView.getText().toString();
        if (title.length() > 255){
            title = title.substring(0, 255);
        }
        event.setTitle(title);

        String description = descriptionView.getText().toString();
        if (description.length() > 4000){
            description = description.substring(0, 4000);
        }
        event.setDescription(description);

        requestHandler.setParameter(Const.EVENT_TITLE, title);
        requestHandler.setParameter(Const.EVENT_COMMENT, description);
        requestHandler.setParameter(Const.EVENT_START, event.getDtstart());
        requestHandler.setParameter(Const.EVENT_END, event.getDtend());
        requestFetch();
    }

    @Override
    public void onFetch(CreateEvent response) {
        String nr = response.getEventNr();
        event.setNr(nr);
        TcaDb.getInstance(this).calendarDao().insert(event);
        finish();
    }

    private void showErrorDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(R.string.error);
        builder.setIcon(R.drawable.ic_error_outline);
        builder.setNeutralButton(R.string.ok, null);
        builder.show();
    }
}
