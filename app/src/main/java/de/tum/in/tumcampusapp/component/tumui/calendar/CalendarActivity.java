package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.Lifecycle;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequestFetchListener;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarRowSet;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.DeleteEvent;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static de.tum.in.tumcampusapp.utils.CacheManager.VALIDITY_FIVE_DAYS;
import static de.tum.in.tumcampusapp.utils.Const.CALENDAR_ID_PARAM;

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
public class CalendarActivity extends ActivityForAccessingTumOnline<CalendarRowSet> implements OnClickListener, MonthLoader.MonthChangeListener, WeekView.EventClickListener, LimitPickerDialogListener {

    /**
     * The space between the first and the last date
     */
    public static final int MONTH_AFTER = 3;
    public static final int MONTH_BEFORE = 2;
    public static final String EVENT_TIME = "event_time";
    private static final int REQUEST_SYNC = 0;
    private static final int REQUEST_DELETE = 1;
    private static final String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
                                                          Manifest.permission.WRITE_CALENDAR};
    private static final int TIME_TO_SYNC_CALENDAR = VALIDITY_FIVE_DAYS;
    private final LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);
    private CalendarController calendarController;
    /**
     * Used as a flag, if there are results fetched from internet
     */
    private boolean isFetched;
    private boolean mWeekMode;
    private DateTime mShowDate;
    private WeekView mWeekView;
    private MenuItem menuItemSwitchView;
    private MenuItem menuItemFilterCanceled;

    private CalendarDetailsFragment detailsFragment;

    public CalendarActivity() {
        super(TUMOnlineConst.Companion.getCALENDER(), R.layout.activity_calendar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.weekView);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);
        mWeekView.setOnEventClickListener(this);

        // Get time to show e.g. a lectures starting time or 0 for now
        Intent i = getIntent();
        mShowDate = DateTime.now();
        if (i != null && i.hasExtra(EVENT_TIME)) {
            long time = i.getLongExtra(EVENT_TIME, 0);
            mShowDate = mShowDate.withMillis(time);
        }

        //Get setting from sharedprefs and refresh the view with everything
        this.mWeekMode = Utils.getSettingBool(this, Const.CALENDAR_WEEK_MODE, false);
        this.refreshWeekView();

        calendarController = new CalendarController(this);

        // Set the time space between now and after this date and before this
        // Dates before the current date
        requestHandler.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
        // Dates after the current date
        requestHandler.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));

        if (new SyncManager(this).needSync(Const.SYNC_CALENDAR_IMPORT, TIME_TO_SYNC_CALENDAR)) {
            requestFetch();
        } else {
            isFetched = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sync_calendar, menu);
        menuItemSwitchView = menu.findItem(R.id.action_switch_view_mode);
        menuItemFilterCanceled = menu.findItem(R.id.action_calendar_filter_canceled);
        //Refresh the icon according to us having day or weekview
        this.refreshWeekView();

        // Initiate checkboxes for filter in top menu
        initFilterCheckboxes();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
        MenuItem menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);

        // the Calendar export is not supported for API < 14
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
        //Check Calendar permission for Android 6.0
        if (!isPermissionGranted(REQUEST_SYNC)) {
            return;
        }

        showLoadingStart();
        Completable.fromAction(() -> CalendarController.syncCalendar(this))
                   .compose(provider.bindToLifecycle())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(() -> {
                       if (!isFinishing()) {
                           AlertDialog.Builder builder = new AlertDialog.Builder(this);
                           builder.setMessage(CalendarActivity.this.getString(R.string.dialog_show_calendar))
                                  .setPositiveButton(CalendarActivity.this.getString(R.string.yes), this)
                                  .setNegativeButton(CalendarActivity.this.getString(R.string.no), this)
                                  .show();
                           showLoadingEnded();
                       }
                   });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        switch (i) {
            case R.id.action_switch_view_mode:
                mWeekMode = !mWeekMode;
                Utils.setSetting(this, Const.CALENDAR_WEEK_MODE, mWeekMode);
                this.refreshWeekView();
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
                startActivity(new Intent(this, CreateEventActivity.class));
                return true;
            case R.id.action_calendar_filter_canceled:
                item.setChecked(!item.isChecked());
                applyFilterCanceled(item.isChecked());
                return true;
            case R.id.action_calendar_filter_hour_limit:
                showHourLimitFilterDialog();
                return true;
            case R.id.action_update_calendar:
                requestFetch(true);
                refreshWeekView();
                return true;
            default:
                isFetched = false;
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFetch(final CalendarRowSet rawResponse) {
        // parsing and saving xml response
        isFetched = true;
        Completable.fromAction(() -> calendarController.importCalendar(rawResponse))
                   .compose(provider.bindToLifecycle())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(() -> {
                       showLoadingEnded();
                       // update the action bar to display the enabled menu options
                       CalendarActivity.this.invalidateOptionsMenu();
                       startService(new Intent(CalendarActivity.this, CalendarController.QueryLocationsService.class));
                   });
    }

    /**
     * Load up the week view with correct settingsPrefix
     */
    private void refreshWeekView() {
        setupDateTimeInterpreter(mWeekMode);
        int icon;
        if (mWeekMode) {
            icon = R.drawable.ic_action_day_view;
            mWeekView.setNumberOfVisibleDays(5);
            // Lets change some dimensions to best fit the view.
            mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
            mWeekView.setXScrollingSpeed(1);
        } else {
            icon = R.drawable.ic_action_week_view;
            mWeekView.setNumberOfVisibleDays(1);
            // Lets change some dimensions to best fit the view.
            mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            mWeekView.setXScrollingSpeed(0.4f);
        }

        // Go to current date or the one given in the intent
        mWeekView.goToDate(mShowDate.toGregorianCalendar());
        mWeekView.goToHour(mShowDate.getHourOfDay());

        // When called from constructor this member is not yet initialized
        if (menuItemSwitchView != null) {
            menuItemSwitchView.setIcon(icon);
        }
    }

    /**
     * Check Calendar permission for Android 6.0
     *
     * @param id the request id
     * @return If the calendar permission was granted
     */
    private boolean isPermissionGranted(int id) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {

                // Display an AlertDialog with an explanation and a button to trigger the request.
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.permission_calendar_explanation))
                        .setPositiveButton(R.string.ok, (dialog, id1) -> ActivityCompat
                                .requestPermissions(CalendarActivity.this, PERMISSIONS_CALENDAR, id))
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
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with the events of the month to display
        List<WeekViewEvent> events = new ArrayList<>();

        DateTime begin = new DateTime().withDate(newYear, newMonth, 1);

        int daysInMonth = begin.dayOfMonth()
                               .getMaximumValue();

        DateTime end = new DateTime().withDate(newYear, newMonth, daysInMonth);

        List<CalendarItem> calendarItems = calendarController.getFromDbBetweenDates(begin, end);
        for (CalendarItem calendarItem : calendarItems) {
            if (Utils.getSettingBool(this, Const.CALENDAR_FILTER_CANCELED, true) || !calendarItem.getStatus()
                                                                                                 .equals("CANCEL")) {
                events.add(new IntegratedCalendarEvent(calendarItem, this));
            }
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
                String dateString = DateUtils.formatDateTime(getApplicationContext(),
                                                             date.getTimeInMillis(), DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_NO_YEAR);

                return weekDay.toUpperCase(Locale.getDefault()) + ' ' + dateString;
            }

            @Override
            public String interpretTime(int hour, int minutes) {
                DateTimeFormatter hourFormat = DateTimeFormat.forPattern("HH:mm")
                                                             .withLocale(Locale.getDefault());
                DateTime time = new DateTime().withTime(hour, minutes, 0, 0);
                return hourFormat.print(time);
            }
        });
    }

    /**
     * option to delete is shown to the user for every event that does not contain a url.
     * (it is assumed that this is actually an event that was created by the user)
     */
    protected void deleteEvent(final String nr) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.event_delete_title);
        dialog.setMessage(R.string.delete_event_info);
        dialog.setPositiveButton(R.string.delete, (dialog1, which) -> {
            TUMOnlineRequest<DeleteEvent> request = new TUMOnlineRequest<>(
                    TUMOnlineConst.Companion.getDELETE_EVENT(), this, true);
            request.setParameter("pTerminNr", nr);
            request.fetchInteractive(this, new TUMOnlineRequestFetchListener<DeleteEvent>() {
                @Override
                public void onNoInternetError() {
                    Toast.makeText(getApplicationContext(), "Error: you are not connected to the internet", Toast.LENGTH_SHORT)
                         .show();
                }

                @Override
                public void onFetch(DeleteEvent response) {
                    detailsFragment.dismiss();
                    TcaDb.getInstance(getApplicationContext())
                         .calendarDao()
                         .delete(nr);
                    refreshWeekView();
                    Toast.makeText(getApplicationContext(), R.string.delete_event_confirmation, Toast.LENGTH_SHORT)
                         .show();
                }

                @Override
                public void onFetchCancelled() {
                    Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_SHORT)
                         .show();
                }

                @Override
                public void onFetchError(String errorReason) {
                    Toast.makeText(getApplicationContext(), R.string.delete_event_error, Toast.LENGTH_LONG)
                         .show();
                }

                @Override
                public void onNoDataToShow() {
                    Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_SHORT)
                         .show();
                }
            });
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    @Override
    public void onEventClick(WeekViewEvent weekViewEvent, RectF rectF) {
        detailsFragment = new CalendarDetailsFragment();
        Bundle bundle = new Bundle();
        CalendarItem item = calendarController.getCalendarItemByStartAndEndTime(
                new DateTime(weekViewEvent.getStartTime()),
                new DateTime(weekViewEvent.getEndTime()));
        bundle.putString(CALENDAR_ID_PARAM, item.getNr());
        detailsFragment.setArguments(bundle);
        detailsFragment.show(getSupportFragmentManager(), null);
    }

    protected void editEvent(final CalendarItem calendarItem) {
        Bundle bundle = new Bundle();
        bundle.putString(Const.EVENT_TITLE, calendarItem.getTitle());
        bundle.putString(Const.EVENT_COMMENT, calendarItem.getDescription());
        bundle.putString(Const.EVENT_START, DateTimeUtils.INSTANCE.getDateTimeString(calendarItem.getDtstart()));
        bundle.putString(Const.EVENT_END, DateTimeUtils.INSTANCE.getDateTimeString(calendarItem.getDtend()));
        bundle.putString(Const.EVENT_NR, calendarItem.getNr());
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        detailsFragment.dismiss();
    }

    protected void onResume() {
        super.onResume();
        refreshWeekView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    protected int calcHourHeightToFit(int min, int max) {
        // get the height of the weekView and subtract the height of its header
        // to get height of actual calendar section, then divide by 24 to get height of a single hour
        return (mWeekView.getMeasuredHeight()             // height of weekView
                - mWeekView.getTextSize()                 // height of text in header of weekView
                - (3 * mWeekView.getHeaderRowPadding()))    // height of padding above and below text in header
               / (max - min);                             // amount of hours
    }

    protected void initFilterCheckboxes() {
        boolean settings = Utils.getSettingBool(this, Const.CALENDAR_FILTER_CANCELED, true);
        menuItemFilterCanceled.setChecked(settings);
        applyFilterCanceled(settings);

        int savedMin = Integer.parseInt(Utils.getSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MIN, Const.CALENDAR_FILTER_HOUR_LIMIT_MIN_DEFAULT));
        int savedMax = Integer.parseInt(Utils.getSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX_DEFAULT));
        applyFilterLimitHours(savedMin, savedMax);
    }

    protected void applyFilterCanceled(boolean val) {
        Utils.setSetting(this, Const.CALENDAR_FILTER_CANCELED, val);
        onResume();
    }

    protected void hourHeightFitScreen() {
        int minHour = Integer.parseInt(Utils.getSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MIN, Const.CALENDAR_FILTER_HOUR_LIMIT_MIN_DEFAULT));
        int maxHour = Integer.parseInt(Utils.getSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX_DEFAULT));
        int hourHeight = calcHourHeightToFit(minHour, maxHour);
        mWeekView.setHourHeight(hourHeight);
    }

    protected void applyFilterLimitHours(int min, int max) {
        // Get old max value to check, if new min will be bigger, in which case the order of setting the new values must be reversed
        int oldMax = Integer.parseInt(Utils.getSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX, "0"));

        Utils.setSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MIN, Integer.toString(min));
        Utils.setSetting(this, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX, Integer.toString(max));

        if (min >= oldMax) {
            mWeekView.setMaxTime(max);
            mWeekView.setMinTime(min);
        } else {
            mWeekView.setMinTime(min);
            mWeekView.setMaxTime(max);
        }
        hourHeightFitScreen();
    }

    protected void showHourLimitFilterDialog() {
        LimitPickerDialog dialog = new LimitPickerDialog(this);
        dialog.addListener(this);
        dialog.show();
    }

    @Override
    public void onSelected(int min, int max) {
        applyFilterLimitHours(min, max);
    }
}

