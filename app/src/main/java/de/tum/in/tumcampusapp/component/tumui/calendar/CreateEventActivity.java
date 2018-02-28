package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CreateEvent;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.Utils;

public class CreateEventActivity extends ActivityForAccessingTumOnline<CreateEvent> {
    Calendar start, end;

    public CreateEventActivity(){
        super(TUMOnlineConst.Companion.getCREATE_EVENT(), R.layout.create_event_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initStartEndDates();
        setDateAndTimeListeners();
        findViewById(R.id.create_event_button).setOnClickListener(view -> createEvent());
    }

    @Override
    public void onFetch(CreateEvent response) {
        Utils.log("Nr of newly created event: " + response.getNr());
        finish();
    }

    private void initStartEndDates(){
        // initial start: round up to the next full hour
        start = Calendar.getInstance();
        start.add(Calendar.HOUR_OF_DAY, 1);
        start.set(Calendar.MINUTE, 0);

        // initial length: 1 hour
        end = Calendar.getInstance();
        end.setTimeInMillis(start.getTimeInMillis());
        end.add(Calendar.HOUR_OF_DAY, 1);
    }

    private void setDateAndTimeListeners(){

        // DATE
        findViewById(R.id.event_start_date).setOnClickListener(view -> {
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                start.set(year, month, dayOfMonth);
                if(end.before(start)){
                    end.set(year, month, dayOfMonth);
                }
                updateDateViews();
            }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH)).show();

        });
        findViewById(R.id.event_end_date).setOnClickListener(view -> {
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                long oldValue = end.getTimeInMillis();
                end.set(year, month, dayOfMonth);
                if(end.before(start)){
                    end.setTimeInMillis(oldValue);
                }
                updateDateViews();
            }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH)).show();
        });

        // TIME
        findViewById(R.id.event_start_time).setOnClickListener(view -> {
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                long eventLength = end.getTimeInMillis() - start.getTimeInMillis();
                start.set(Calendar.HOUR_OF_DAY, hour);
                start.set(Calendar.MINUTE, minute);
                end.setTimeInMillis(start.getTimeInMillis() + eventLength);
                updateTimeViews();
            }, start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), true).show();
        });

        findViewById(R.id.event_end_time).setOnClickListener(view -> {
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                long oldValue = end.getTimeInMillis();
                end.set(Calendar.HOUR_OF_DAY, hour);
                end.set(Calendar.MINUTE, minute);
                if(end.before(start)){
                    end.setTimeInMillis(oldValue);
                }
                updateTimeViews();
            }, end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE), true).show();
        });
    }

    private void updateTimeViews(){
        ((TextView)findViewById(R.id.event_start_time))
                .setText(getString(start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE)));
        ((TextView)findViewById(R.id.event_end_time))
                .setText(getString(R.string.time, end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE)));
    }
    private void updateDateViews(){
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);

        ((TextView)findViewById(R.id.event_start_date))
                .setText(format.format(start.getTime()));

        ((TextView)findViewById(R.id.event_end_date))
                .setText(format.format(end.getTime()));
    }

    private void createEvent(){
        // do another check
        if(!start.before(end) && start.before(new Date())){
            showError();
            return;
        }

        String from = DateUtils.getDateTimeString(start.getTime());
        String to = DateUtils.getDateTimeString(end.getTime());

        String title = ((EditText)findViewById(R.id.event_title)).getText().toString();
        if(title.length() > 255){
            title = title.substring(0, 255);
        }
        String description = ((EditText)findViewById(R.id.event_description)).getText().toString();
        if(description.length() > 4000){
            description = description.substring(0, 4000);
        }

        finish(); // only for testing, later on only finish the activity after request was sent

        // TODO create Event (see wiki)
        // + add to db ? or refresh (this might take too long for the user)?
    }

    private void showError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please make sure the start time of the event is before the end time");
        builder.setIcon(R.drawable.ic_error_outline);
        builder.setNeutralButton(R.string.ok, null);
        builder.show();
    }
}
