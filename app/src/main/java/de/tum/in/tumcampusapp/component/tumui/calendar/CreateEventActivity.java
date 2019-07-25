package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.UnknownHostException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.exception.RequestLimitReachedException;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CreateEventResponse;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.DeleteEventResponse;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Allows the user to create (and edit) a private event in TUMonline.
 */
public class CreateEventActivity extends ActivityForAccessingTumOnline<CreateEventResponse> {

    private DateTime start;
    private DateTime end;

    private boolean isEditing;
    private TextView titleView;
    private TextView descriptionView;
    private TextView startDateView;
    private TextView startTimeView;
    private TextView endDateView;
    private TextView endTimeView;
    private MaterialButton createButton;
    private CalendarItem event;

    public CreateEventActivity() {
        super(R.layout.activity_create_event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        if (getSupportActionBar() != null) {
            Drawable closeIcon = ContextCompat.getDrawable(this, R.drawable.ic_clear);
            int color = ContextCompat.getColor(this, R.color.color_primary);
            if (closeIcon != null) {
                closeIcon.setTint(color);
                getSupportActionBar().setHomeAsUpIndicator(closeIcon);
            }
        }

        if (getSwipeRefreshLayout() != null) {
            // We only use the SwipeRefreshLayout to indicate progress, not to allow
            // the user to pull to refresh.
            getSwipeRefreshLayout().setEnabled(false);
        }

        titleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = s.toString().isEmpty();
                float alpha = isEmpty ? 0.5f : 1.0f;
                createButton.setEnabled(!isEmpty);
                createButton.setAlpha(alpha);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // an event with extras can either be editing an existing event
            // or adding a new event from Tickets & Events
            isEditing = extras.getBoolean(Const.EVENT_EDIT);
            if (isEditing) {
                createButton.setText(R.string.event_save_edit_button);
            }
            titleView.setText(extras.getString(Const.EVENT_TITLE));
            descriptionView.setText(extras.getString(Const.EVENT_COMMENT));
        } else {
            titleView.requestFocus();
            showKeyboard();
        }

        initStartEndDates(extras);
        setDateAndTimeListeners();

        createButton.setOnClickListener(view -> {
            if (end.isBefore(start)) {
                showErrorDialog(getString(R.string.create_event_time_error));
                return;
            }
            if (isEditing) {
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
        LocalDate initialDate = (LocalDate) extras.getSerializable(Const.DATE);
        start = (DateTime) extras.getSerializable(Const.EVENT_START);
        end = (DateTime) extras.getSerializable(Const.EVENT_END);

        if (start == null || end == null) {
            if (initialDate == null) {
                throw new IllegalStateException("No date provided for CreateEventActivity");
            }

            // We’re creating a new event, so we set the start and end time to the next full hour
            start = initialDate.toDateTimeAtCurrentTime()
                    .plusHours(1)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);
            end = start.plusHours(1);
        }

        updateDateViews();
        updateTimeViews();
    }

    private void setDateAndTimeListeners() {
        // DATE
        
        // Month +/- 1 is needed because the date picker uses zero-based month values while DateTime
        // starts counting months at 1.
        startDateView.setOnClickListener(view -> {
            hideKeyboard();
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                start = start.withDate(year, month + 1, dayOfMonth);
                if (end.isBefore(start)) {
                    end = end.withDate(year, month + 1, dayOfMonth);
                }
                updateDateViews();
            }, start.getYear(), start.getMonthOfYear() - 1, start.getDayOfMonth()).show();

        });
        endDateView.setOnClickListener(view -> {
            hideKeyboard();
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                end = end.withDate(year, month + 1, dayOfMonth);
                updateDateViews();
            }, start.getYear(), start.getMonthOfYear() - 1, start.getDayOfMonth()).show();
        });

        // TIME
        startTimeView.setOnClickListener(view -> {
            hideKeyboard();
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                long eventLength = end.getMillis() - start.getMillis();
                start = start.withHourOfDay(hour)
                             .withMinuteOfHour(minute);
                end = end.withMillis(start.getMillis() + eventLength);
                updateTimeViews();
            }, start.getHourOfDay(), start.getMinuteOfHour(), true).show();
        });

        endTimeView.setOnClickListener(view -> {
            hideKeyboard();
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                end = end.withHourOfDay(hour)
                         .withMinuteOfHour(minute);
                updateTimeViews();
            }, end.getHourOfDay(), end.getMinuteOfHour(), true).show();
        });
    }

    private void updateTimeViews() {
        DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm")
                                                 .withLocale(Locale.getDefault());
        startTimeView.setText(format.print(start));
        endTimeView.setText(format.print(end));
    }

    private void updateDateViews() {
        DateTimeFormatter format = DateTimeFormat.forPattern("EEE, dd.MM.yyyy")
                                                 .withLocale(Locale.getDefault());
        startDateView.setText(format.print(start));
        endDateView.setText(format.print(end));
    }

    private void editEvent() {
        final String eventId = getIntent().getStringExtra(Const.EVENT_NR);
        if (eventId == null) {
            return;
        }

        // Because we don't show a loading screen for the delete request (only for the create
        // request), we use a short Toast to let the user know that something is happening.
        Toast.makeText(this, R.string.updating_event, Toast.LENGTH_SHORT).show();

        getApiClient()
                .deleteEvent(eventId)
                .enqueue(new Callback<DeleteEventResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeleteEventResponse> call,
                                           @NonNull Response<DeleteEventResponse> response) {
                        if (response.isSuccessful()) {
                            Utils.log("Event successfully deleted (now creating the edited version)");
                            TcaDb.Companion.getInstance(CreateEventActivity.this).calendarDao().delete(eventId);
                            createEvent();
                        } else {
                            Utils.showToast(CreateEventActivity.this, R.string.error_unknown);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeleteEventResponse> call,
                                          @NonNull Throwable t) {
                        Utils.log(t);
                        displayErrorMessage(t);
                    }
                });
    }

    private void displayErrorMessage(Throwable t) {
        int messageResId;

        if (t instanceof UnknownHostException) {
            messageResId = R.string.error_no_internet_connection;
        } else if (t instanceof RequestLimitReachedException) {
            messageResId = R.string.error_request_limit_reached;
        } else {
            messageResId = R.string.error_unknown;
        }

        Utils.showToast(this, messageResId);
    }

    private void createEvent() {
        event = new CalendarItem();
        event.setDtstart(start);
        event.setDtend(end);

        String title = titleView.getText().toString();
        if (title.length() > 255) {
            title = title.substring(0, 255);
        }
        event.setTitle(title);

        String description = descriptionView.getText().toString();
        if (description.length() > 4000) {
            description = description.substring(0, 4000);
        }
        event.setDescription(description);

        Call<CreateEventResponse> apiCall = getApiClient().createEvent(event, null);
        fetch(apiCall);
    }

    @Override
    public void onDownloadSuccessful(@NonNull CreateEventResponse response) {
        String nr = response.getEventId();
        event.setNr(nr);
        TcaDb.Companion.getInstance(this).calendarDao().insert(event);
        finish();
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();

        boolean handled = handleOnBackPressed();
        if (handled) {
            finish();
        } else {
            displayCloseDialog();
        }
    }

    private void showKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(titleView.getWindowToken(),0);
        }
    }

    private boolean handleOnBackPressed() {
        String title = titleView.getText().toString();
        String description = descriptionView.getText().toString();

        // TODO: If the user is in edit mode, check whether any data was changed.
        return title.isEmpty() && description.isEmpty();
    }

    private void displayCloseDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.discard_changes_question)
                .setNegativeButton(R.string.discard, (dialogInterface, which) -> finish())
                .setPositiveButton(R.string.keep_editing, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showErrorDialog(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setMessage(message)
            .setIcon(R.drawable.ic_error_outline)
            .setPositiveButton(R.string.ok, null)
            .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

}
