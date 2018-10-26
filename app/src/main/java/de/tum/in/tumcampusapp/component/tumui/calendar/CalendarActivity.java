package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.notifications.persistence.NotificationType;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.Event;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.EventsResponse;
import de.tum.in.tumcampusapp.component.ui.transportation.TransportController;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
public class CalendarActivity extends ActivityForAccessingTumOnline<EventsResponse>
        implements OnClickListener, MonthLoader.MonthChangeListener, WeekView.EventClickListener,
                   CalendarDetailsFragment.OnEventInteractionListener {

    private static final int REQUEST_SYNC = 0;
    private static final int REQUEST_DELETE = 1;
    private static final String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
                                                          Manifest.permission.WRITE_CALENDAR};

    private CalendarController calendarController;
    private boolean isPaused;

    /**
     * Used as a flag, if there are results fetched from internet
     */
    private boolean isFetched;
    private boolean mWeekMode;
    private DateTime mShowDate;
    private MenuItem menuItemSwitchView;
    private MenuItem menuItemFilterCanceled;

    private WeekView mWeekView;
    private MaterialButton mTodayButton;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private CalendarDetailsFragment detailsFragment;

    public CalendarActivity() {
        super(R.layout.activity_calendar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeekView = findViewById(R.id.weekView);
        calendarController = new CalendarController(this);
        isPaused = false;

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setScrollListener((newFirstVisibleDay, oldFirstVisibleDay) -> {
            LocalDate visibleDay = new LocalDate(newFirstVisibleDay);
            LocalDate today = LocalDate.now();
            boolean isToday = visibleDay.isEqual(today);

            mTodayButton.setVisibility(isToday ? View.GONE : View.VISIBLE);
            TransitionManager.beginDelayedTransition(getSwipeRefreshLayout());
        });

        mTodayButton = findViewById(R.id.todayButton);
        mTodayButton.setOnClickListener(view -> refreshWeekView());

        // The week view adds a horizontal bar below the Toolbar. When refreshing, the refresh
        // spinner covers it. Therefore, we adjust the spinner's end position.
        int startOffset = getSwipeRefreshLayout().getProgressViewStartOffset();
        int endOffset = getSwipeRefreshLayout().getProgressViewEndOffset();
        getSwipeRefreshLayout().setProgressViewOffset(false, startOffset, endOffset);

        // Get time to show e.g. a lectures starting time or 0 for now
        Intent intent = getIntent();
        mShowDate = DateTime.now();
        if (intent != null && intent.hasExtra(Const.EVENT_TIME)) {
            long time = intent.getLongExtra(Const.EVENT_TIME, 0);
            mShowDate = mShowDate.withMillis(time);

            String eventId = intent.getStringExtra(Const.KEY_EVENT_ID);
            if (eventId != null) {
                openEvent(eventId);
            }
        }

        // Get setting from sharedprefs and refresh the view with everything
        mWeekMode = Utils.getSettingBool(this, Const.CALENDAR_WEEK_MODE, false);

        disableRefresh();
        loadEvents(CacheControl.USE_CACHE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        isPaused = false;
        refreshWeekView();
    }

    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onRefresh() {
        loadEvents(CacheControl.BYPASS_CACHE);
    }

    private void loadEvents(CacheControl cacheControl) {
        Call<EventsResponse> apiCall = apiClient.getCalendar(cacheControl);
        fetch(apiCall);
    }

    @Override
    protected void onDownloadSuccessful(@NonNull EventsResponse response) {
        isFetched = true;

        List<Event> events = response.getEvents();
        if (events == null) {
            return;
        }

        scheduleNotifications(events);

        mDisposable.add(
                Completable
                        .fromAction(() -> calendarController.importCalendar(events))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            // Update the action bar to display the enabled menu options
                            invalidateOptionsMenu();
                            Intent intent = new Intent(
                                    this, CalendarController.QueryLocationsService.class);
                            startService(intent);
                        })
        );
    }

    private void scheduleNotifications(@NonNull List<Event> events) {
        if (calendarController.hasNotificationsEnabled()) {
            calendarController.scheduleNotifications(events);
        }

        TransportController transportController = new TransportController(this);
        if (transportController.hasNotificationsEnabled()) {
            transportController.scheduleNotifications(events);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sync_calendar, menu);

        menuItemSwitchView = menu.findItem(R.id.action_switch_view_mode);
        menuItemFilterCanceled = menu.findItem(R.id.action_calendar_filter_canceled);

        // Refresh the icon according to us having day or week view
        refreshWeekView();

        // Initiate checkboxes for filter in top menu
        initFilterCheckboxes();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
        MenuItem menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);

        menuItemExportGoogle.setEnabled(isFetched);
        menuItemDeleteCalendar.setEnabled(isFetched);

        boolean bed = Utils.getSettingBool(this, Const.SYNC_CALENDAR, false);
        menuItemExportGoogle.setVisible(!bed);
        menuItemDeleteCalendar.setVisible(bed);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Asynchronous task for exporting the calendar to a local Google calendar
     */
    private void exportCalendarToGoogle() {
        // Check Calendar permission for Android 6.0
        if (!isPermissionGranted(REQUEST_SYNC)) {
            return;
        }

        mDisposable.add(
                Completable.fromAction(() -> CalendarController.syncCalendar(this))
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .subscribe(() -> {
                               if (!isFinishing()) {
                                   new AlertDialog.Builder(this)
                                           .setMessage(getString(R.string.dialog_show_calendar))
                                           .setNegativeButton(getString(R.string.no), null)
                                           .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                               displayCalendarOnGoogleCalendar();
                                           })
                                           .show();
                               }
                           }, throwable -> {
                               Utils.log(throwable);
                               Utils.showToast(this, R.string.export_to_google_error);
                           })
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();
        switch (i) {
            case R.id.action_switch_view_mode:
                mWeekMode = !mWeekMode;
                Utils.setSetting(this, Const.CALENDAR_WEEK_MODE, mWeekMode);
                refreshWeekView();
                return true;
            case R.id.action_export_calendar:
                exportCalendarToGoogle();

                // Enable automatic calendar synchronisation
                Utils.setSetting(this, Const.SYNC_CALENDAR, true);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_delete_calendar:
                deleteCalendarFromGoogle();
                return true;
            case R.id.action_create_event:
                LocalDate currentDate = new LocalDate(mWeekView.getFirstVisibleDay());
                Intent intent = new Intent(this, CreateEventActivity.class);
                intent.putExtra(Const.DATE, currentDate);
                startActivity(intent);
                return true;
            case R.id.action_calendar_filter_canceled:
                item.setChecked(!item.isChecked());
                applyFilterCanceled(item.isChecked());
                return true;
            case R.id.action_update_calendar:
                loadEvents(CacheControl.BYPASS_CACHE);
                refreshWeekView();
                return true;
            default:
                isFetched = false;
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load up the week view with correct settingsPrefix
     */
    private void refreshWeekView() {
        setupDateTimeInterpreter(mWeekMode);
        int icon;

        if (mWeekMode) {
            icon = R.drawable.ic_outline_calendar_view_day_24px;
            mWeekView.setNumberOfVisibleDays(5);
            // Lets change some dimensions to best fit the view.
            mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            mWeekView.setXScrollingSpeed(1);
        } else {
            icon = R.drawable.ic_outline_view_column_24px;
            mWeekView.setNumberOfVisibleDays(1);
            // Lets change some dimensions to best fit the view.
            mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
            mWeekView.setXScrollingSpeed(0.4f);
        }

        // Go to current date or the one given in the intent
        mWeekView.goToDate(mShowDate.toGregorianCalendar());
        mWeekView.goToHour(mShowDate.getHourOfDay());

        // When called from constructor this member is not yet initialized
        if (menuItemSwitchView != null) {
            menuItemSwitchView.setIcon(icon);
        }

        mWeekView.invalidate();
    }

    /**
     * Check Calendar permission for Android 6.0
     *
     * @param id the request id
     * @return If the calendar permission was granted
     */
    private boolean isPermissionGranted(int id) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {

                // Display an AlertDialog with an explanation and a button to trigger the request.
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.permission_calendar_explanation))
                        .setPositiveButton(R.string.ok, (dialog, id1) ->
                                ActivityCompat.requestPermissions(this, PERMISSIONS_CALENDAR, id))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS_CALENDAR, id);
            }
        }

        return false;
    }

    /**
     * Starts the Google calendar Activity to display the exported calendar.
     */
    private void displayCalendarOnGoogleCalendar() {
        // displaying Calendar
        long startMillis = DateTime.now()
                                   .getMillis();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        startActivity(intent);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            displayCalendarOnGoogleCalendar();
        }
    }

    /**
     * Async task for deleting the calendar from local Google calendar
     */
    private void deleteCalendarFromGoogle() {
        //Check Calendar permission for Android 6.0
        if (!isPermissionGranted(REQUEST_DELETE)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_delete_calendar))
               .setPositiveButton(getString(R.string.yes), (arg0, arg1) -> {
                   int deleted = CalendarController.deleteLocalCalendar(this);
                   Utils.setSetting(CalendarActivity.this, Const.SYNC_CALENDAR, false);
                   this.invalidateOptionsMenu();
                   if (deleted > 0) {
                       Utils.showToast(this, R.string.calendar_deleted_toast);
                   } else {
                       Utils.showToast(this, R.string.calendar_not_existing_toast);
                   }
               })
               .setNegativeButton(getString(R.string.no), null)
               .show();
    }

    @Override
    public List<WeekViewDisplayable> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with the events of the month to display
        DateTime begin = new DateTime().withDate(newYear, newMonth, 1);
        int daysInMonth = begin.dayOfMonth()
                               .getMaximumValue();
        DateTime end = new DateTime().withDate(newYear, newMonth, daysInMonth);
        return prepareCalendarItems(begin, end);
    }

    private List<WeekViewDisplayable> prepareCalendarItems(DateTime begin, DateTime end) {
        boolean showCancelledEvents = Utils.getSettingBool(this, Const.CALENDAR_FILTER_CANCELED, true);
        List<CalendarItem> calendarItems = showCancelledEvents
                ? calendarController.getFromDbBetweenDates(begin, end)
                : calendarController.getFromDbNotCancelledBetweenDates(begin, end);
        return mergeSimilarCalendarItems(calendarItems);
    }

    /**
     * Creates one event out of multiple instances of the same event that have different locations.
     * List must already be sorted so that event duplicates are right after each other.
     */
    private List<WeekViewDisplayable> mergeSimilarCalendarItems(List<CalendarItem> calendarItems) {
        List<WeekViewDisplayable> events = new ArrayList<>();
        for (int i = 0; i < calendarItems.size(); i++) {
            CalendarItem calendarItem = calendarItems.get(i);
            StringBuilder location = new StringBuilder();
            location.append(calendarItem.getLocation());
            while (i + 1 < calendarItems.size()
                    && calendarItem.isSameEventButForLocation(calendarItems.get(i + 1))) {
                i++;
                location.append(" + ");
                location.append(calendarItems.get(i).getLocation());

            }
            calendarItem.setLocation(location.toString());
            events.add(new IntegratedCalendarEvent(calendarItem, this));
        }
        return events;
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     *
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                final String weekDayFormat;
                if (shortDate) { // 3 characters
                    weekDayFormat = "E";
                } else {
                    weekDayFormat = "EEEE";
                }
                String weekDay = DateTimeFormat.forPattern(weekDayFormat)
                                               .withLocale(Locale.getDefault())
                                               .print(new DateTime(date.getTimeInMillis()));
                String dateString = DateUtils.formatDateTime(
                        CalendarActivity.this, date.getTimeInMillis(),
                        DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_NO_YEAR);

                return weekDay.toUpperCase(Locale.getDefault()) + ' ' + dateString;
            }

            @Override
            public String interpretTime(int hour) {
                DateTimeFormatter hourFormat = DateTimeFormat.forPattern("HH:mm")
                                                             .withLocale(Locale.getDefault());
                DateTime time = new DateTime().withTime(hour, 0, 0, 0);
                return hourFormat.print(time);
            }
        });
    }

    @Override
    public void onEventDeleted(@NotNull String eventId) {
        TcaDb db = TcaDb.getInstance(this);
        db.calendarDao()
          .delete(eventId);

        int id = Integer.parseInt(eventId);
        db.scheduledNotificationsDao()
          .delete(NotificationType.CALENDAR.getId(), id);

        refreshWeekView();
        Utils.showToast(this, R.string.delete_event_confirmation);
    }

    @Override
    public void onEditEvent(@NotNull CalendarItem calendarItem) {
        // TODO: CalendarItem should implement Parcelable
        Bundle bundle = new Bundle();
        bundle.putBoolean(Const.EVENT_EDIT, true);
        bundle.putString(Const.EVENT_TITLE, calendarItem.getTitle());
        bundle.putString(Const.EVENT_COMMENT, calendarItem.getDescription());
        bundle.putSerializable(Const.EVENT_START, calendarItem.getDtstart());
        bundle.putSerializable(Const.EVENT_END, calendarItem.getDtend());
        bundle.putString(Const.EVENT_NR, calendarItem.getNr());
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        detailsFragment.dismiss();
    }

    @Override
    public void onEventClick(WeekViewEvent weekViewEvent, RectF rectF) {
        // Don't call openEvent if the activity is paused.
        if (isPaused) {
            return;
        }
        String eventId = String.valueOf(weekViewEvent.getId());
        openEvent(eventId);
    }
    
    private void openEvent(String eventId) {
        List<CalendarItem> item = calendarController.getCalendarItemsById(eventId);
        if (item == null || item.isEmpty()) {
            return;
        }
        detailsFragment = CalendarDetailsFragment.newInstance(item, this);
        detailsFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Check if we got all Calendar permissions
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        //Rerun the interrupted action
        if (requestCode == REQUEST_SYNC) {
            exportCalendarToGoogle();
        } else if (requestCode == REQUEST_DELETE) {
            deleteCalendarFromGoogle();
        }
    }

    protected void initFilterCheckboxes() {
        boolean showCancelledEvents = Utils.getSettingBool(this, Const.CALENDAR_FILTER_CANCELED, true);
        Utils.log(showCancelledEvents ? "Show cancelled events" : "Hide cancelled events");
        menuItemFilterCanceled.setChecked(showCancelledEvents);
        applyFilterCanceled(showCancelledEvents);
    }

    protected void applyFilterCanceled(boolean val) {
        Utils.setSetting(this, Const.CALENDAR_FILTER_CANCELED, val);
        refreshWeekView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }
}

