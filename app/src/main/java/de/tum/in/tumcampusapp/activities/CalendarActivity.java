package de.tum.in.tumcampusapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.fragments.CalendarDetailsFragment;
import de.tum.in.tumcampusapp.managers.CalendarManager;
import de.tum.in.tumcampusapp.managers.SyncManager;
import de.tum.in.tumcampusapp.models.tumo.CalendarItem;
import de.tum.in.tumcampusapp.models.tumo.CalendarRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;

import static de.tum.in.tumcampusapp.auxiliary.Const.CALENDAR_ID_PARAM;

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
public class CalendarActivity extends ActivityForAccessingTumOnline<CalendarRowSet> implements OnClickListener, MonthLoader.MonthChangeListener, WeekView.EventClickListener {

    /**
     * The space between the first and the last date
     */
    public static final int MONTH_AFTER = 3;
    public static final int MONTH_BEFORE = 0;
    public static final String EVENT_TIME = "event_time";
    private static final int REQUEST_SYNC = 0;
    private static final int REQUEST_DELETE = 1;
    private static final String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
                                                          Manifest.permission.WRITE_CALENDAR};
    private static final int TIME_TO_SYNC_CALENDAR = 604800; // 1 week
    private CalendarManager calendarManager;

    /**
     * Used as a flag, if there are results fetched from internet
     */
    private boolean isFetched;
    private boolean mWeekMode;
    private Calendar mShowDate;
    private MenuItem menuItemSwitchView;
    private WeekView mWeekView;

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
        mShowDate = Calendar.getInstance();
        if (i != null && i.hasExtra(EVENT_TIME)) {
            long time = i.getLongExtra(EVENT_TIME, 0);
            mShowDate.setTime(new Date(time));
        } else {
            mShowDate.setTime(new Date());
        }

        //Get setting from sharedprefs and refresh the view with everything
        this.mWeekMode = Utils.getInternalSettingBool(this, Const.CALENDAR_WEEK_MODE, false);
        this.refreshWeekView();

        calendarManager = new CalendarManager(this);

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
    public void onFetch(final CalendarRowSet rawResponse) {
        // parsing and saving xml response
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                isFetched = true;
            }

            @Override
            protected Void doInBackground(Void... params) {
                calendarManager.importCalendar(rawResponse);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                showLoadingEnded();
                // update the action bar to display the enabled menu options
                CalendarActivity.this.invalidateOptionsMenu();
                startService(new Intent(CalendarActivity.this, CalendarManager.QueryLocationsService.class));
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sync_calendar, menu);
        menuItemSwitchView = menu.findItem(R.id.action_switch_view_mode);

        //Refresh the icon according to us having day or weekview
        this.refreshWeekView();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
        MenuItem menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);

        // the Calendar export is not supported for API < 14
        menuItemExportGoogle.setEnabled(isFetched);
        menuItemDeleteCalendar.setEnabled(isFetched);

        boolean bed = Utils.getInternalSettingBool(this, Const.SYNC_CALENDAR, false);
        menuItemExportGoogle.setVisible(!bed);
        menuItemDeleteCalendar.setVisible(bed);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_switch_view_mode) {
            mWeekMode = !mWeekMode;
            Utils.setInternalSetting(this, Const.CALENDAR_WEEK_MODE, mWeekMode);
            this.refreshWeekView();
            return true;
        } else if (i == R.id.action_export_calendar) {
            exportCalendarToGoogle();

            // Enable automatic calendar synchronisation
            Utils.setInternalSetting(this, Const.SYNC_CALENDAR, true);
            supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.action_delete_calendar) {
            deleteCalendarFromGoogle();
            return true;
        } else {
            isFetched = false;
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load up the week view with correct settings
     */
    private void refreshWeekView() {
        setupDateTimeInterpreter(mWeekMode);
        int icon;
        if (mWeekMode) {
            icon = R.drawable.ic_action_day_view;
            mWeekView.setNumberOfVisibleDays(7);
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

        //Go to current date or the one givin in the intent
        mWeekView.goToDate((Calendar) this.mShowDate.clone()); //Pass a deep copy, as this method changes the hour to 0
        mWeekView.goToHour(this.mShowDate.get(Calendar.HOUR_OF_DAY));

        //When called from constructor this member is not yet initialized
        if (menuItemSwitchView != null) {
            menuItemSwitchView.setIcon(icon);
        }
    }

    /**
     * Asynchronous task for exporting the calendar to a local Google calendar
     */
    private void exportCalendarToGoogle() {
        //Check Calendar permission for Android 6.0
        if (!isPermissionGranted(REQUEST_SYNC)) {
            return;
        }

        AsyncTask<Void, Void, Boolean> backgroundTask;

        backgroundTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                CalendarManager.syncCalendar(CalendarActivity.this);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                // Informs the user about the ongoing action
                if (!CalendarActivity.this.isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                    builder.setMessage(CalendarActivity.this.getString(R.string.dialog_show_calendar))
                           .setPositiveButton(CalendarActivity.this.getString(R.string.yes), CalendarActivity.this)
                           .setNegativeButton(CalendarActivity.this.getString(R.string.no), CalendarActivity.this)
                           .show();
                    showLoadingEnded();
                }
            }

            @Override
            protected void onPreExecute() {
                showLoadingStart();
            }
        };
        backgroundTask.execute();
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
                                .requestPermissions(CalendarActivity.this, PERMISSIONS_CALENDAR, id1))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS_CALENDAR, id);
            }
        }

        return false;
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

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            displayCalendarOnGoogleCalendar();
        }
    }

    /**
     * Starts the Google calendar Activity to display the exported calendar.
     */
    private void displayCalendarOnGoogleCalendar() {
        // displaying Calendar
        Calendar beginTime = Calendar.getInstance();
        long startMillis = beginTime.getTimeInMillis();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        startActivity(intent);
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
                   int deleted = CalendarManager.deleteLocalCalendar(CalendarActivity.this);
                   Utils.setInternalSetting(CalendarActivity.this, Const.SYNC_CALENDAR, false);
                   CalendarActivity.this.invalidateOptionsMenu();
                   if (deleted > 0) {
                       Utils.showToast(CalendarActivity.this, R.string.calendar_deleted_toast);
                   } else {
                       Utils.showToast(CalendarActivity.this, R.string.calendar_not_existing_toast);
                   }
               })
               .setNegativeButton(getString(R.string.no), null)
               .show();
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {

        // Populate the week view with the events of the month to display
        List<WeekViewEvent> events = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();

        //Note the (-1), since the calendar starts with month 0, but we get months starting with 1
        calendar.set(newYear, newMonth - 1, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        //Probably refactor this to a good SQL query
        for (int curDay = 1; curDay <= daysInMonth; curDay++) {
            calendar.set(Calendar.DAY_OF_MONTH, curDay);
            List<CalendarItem> calendarItems = calendarManager.getFromDbForDate(new Date(calendar.getTimeInMillis()));
            for (CalendarItem calendarItem: calendarItems) {
                events.add(new IntegratedCalendarEvent(calendarItem));
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
                if (shortDate) { //Only one character
                    weekDayFormat = "EEEEE";
                } else {
                    weekDayFormat = "EEEE";
                }
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat(weekDayFormat, Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                String dateString = DateUtils.formatDateTime(getApplicationContext(),
                                                             date.getTimeInMillis(), DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_NO_YEAR);

                return weekday.toUpperCase(Locale.getDefault()) + ' ' + dateString;
            }

            @Override
            public String interpretTime(int hour) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, 0);
                DateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return hourFormat.format(cal.getTime());
            }
        });
    }

    @Override
    public void onEventClick(WeekViewEvent weekViewEvent, RectF rectF) {
        CalendarDetailsFragment detailsFragment = new CalendarDetailsFragment();
        Bundle bundle = new Bundle();
        CalendarItem item = calendarManager.getCalendarItemByStartAndEndTime(weekViewEvent.getStartTime(), weekViewEvent.getEndTime());
        bundle.putString(CALENDAR_ID_PARAM, item.getNr());
        detailsFragment.setArguments(bundle);
        detailsFragment.show(getSupportFragmentManager(), null);
    }
}
