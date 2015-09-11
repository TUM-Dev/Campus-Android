package de.tum.in.tumcampus.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.auxiliary.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.managers.CalendarManager;
import de.tum.in.tumcampus.models.managers.DatabaseManager;
import de.tum.in.tumcampus.models.managers.SyncManager;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
public class CalendarActivity extends ActivityForAccessingTumOnline<CalendarRowSet> implements OnClickListener, WeekView.MonthChangeListener {

    /**
     * The space between the first and the last date
     */
    public static final int MONTH_AFTER = 3;
    public static final int MONTH_BEFORE = 0;

    private static final int TIME_TO_SYNC_CALENDAR = 604800; // 1 week
    public static final String EVENT_TIME = "event_time";

    private CalendarManager calendarManager;

    // Objects for disabling or enabling the options menu items
    private MenuItem menuItemExportGoogle;
    private MenuItem menuItemDeleteCalendar;

    /**
     * Used as a flag, if there are results fetched from internet
     */
    private boolean isFetched;
    private boolean mWeekMode = false;
    private long mEventTime = 0;
    private MenuItem menuItemSwitchView;
    private WeekView mWeekView;

    public CalendarActivity() {
        super(TUMOnlineConst.CALENDER, R.layout.activity_calendar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);
        setupDateTimeInterpreter(mWeekMode);
        mWeekView.goToHour(8);

        // Get time to show e.g. a lectures starting time or 0 for now
        Intent i = getIntent();
        if (i != null && i.hasExtra(EVENT_TIME))
            mEventTime = i.getLongExtra(EVENT_TIME, 0);

        //mViewPager = (ViewPager) findViewById(R.id.pager);
        calendarManager = new CalendarManager(this);

        // Set the time space between now and after this date and before this
        // Dates before the current date
        requestHandler.setParameter("pMonateVor", String.valueOf(MONTH_BEFORE));
        // Dates after the current date
        requestHandler.setParameter("pMonateNach", String.valueOf(MONTH_AFTER));

        SQLiteDatabase db = DatabaseManager.getDb(this);
        if (SyncManager.needSync(db, Const.SYNC_CALENDAR_IMPORT, TIME_TO_SYNC_CALENDAR)) {
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
                if (Build.VERSION.SDK_INT >= 14) {
                    invalidateOptionsMenu();
                }
                startService(new Intent(CalendarActivity.this, CalendarManager.QueryLocationsService.class));
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sync_calendar, menu);
        menuItemSwitchView = menu.findItem(R.id.action_switch_view_mode);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // the Calendar export is not supported for API < 14
        if (android.os.Build.VERSION.SDK_INT < 14) {
            menuItemExportGoogle.setVisible(false);
            menuItemDeleteCalendar.setVisible(false);
        } else {
            menuItemExportGoogle = menu.findItem(R.id.action_export_calendar);
            menuItemDeleteCalendar = menu.findItem(R.id.action_delete_calendar);
            menuItemExportGoogle.setEnabled(isFetched);
            menuItemDeleteCalendar.setEnabled(isFetched);

            boolean bed = Utils.getInternalSettingBool(this, Const.SYNC_CALENDAR, false);
            menuItemExportGoogle.setVisible(!bed);
            menuItemDeleteCalendar.setVisible(bed);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_view_mode:
                mWeekMode = !mWeekMode;
                setupDateTimeInterpreter(mWeekMode);
                if (mWeekMode) {
                    menuItemSwitchView.setIcon(R.drawable.ic_action_day_view);
                    mWeekView.setNumberOfVisibleDays(7);
                    // Lets change some dimensions to best fit the view.
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                } else {
                    menuItemSwitchView.setIcon(R.drawable.ic_action_week_view);
                    mWeekView.setNumberOfVisibleDays(1);
                    // Lets change some dimensions to best fit the view.
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                mWeekView.goToHour(8);
                return true;
            case R.id.action_export_calendar:
                exportCalendarToGoogle();

                // Enable automatic calendar synchronisation
                Utils.setInternalSetting(this, Const.SYNC_CALENDAR, true);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_delete_calendar:
                deleteCalendarFromGoogle();
                return true;
            default:
                isFetched = false;
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Asynchronous task for exporting the calendar to a local Google calendar
     */
    void exportCalendarToGoogle() {
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
                            .setNegativeButton(CalendarActivity.this.getString(R.string.no), CalendarActivity.this).show();
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

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            displayCalendarOnGoogleCalendar();
        }
    }

    /**
     * Starts the Google calendar Activity to display the exported calendar.
     */
    @TargetApi(14)
    void displayCalendarOnGoogleCalendar() {
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
    void deleteCalendarFromGoogle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
        builder.setMessage(getString(R.string.dialog_delete_calendar)).setPositiveButton(getString(R.string.yes), new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                int deleted = CalendarManager.deleteLocalCalendar(CalendarActivity.this);
                Utils.setInternalSetting(CalendarActivity.this, Const.SYNC_CALENDAR, false);
                invalidateOptionsMenu();
                if (deleted > 0) {
                    Utils.showToast(CalendarActivity.this, R.string.calendar_deleted_toast);
                } else {
                    Utils.showToast(CalendarActivity.this, R.string.calendar_not_existing_toast);
                }
            }
        }).setNegativeButton(getString(R.string.no), null).show();
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
        for (int curDay = 1; curDay < daysInMonth; curDay++) {
            calendar.set(Calendar.DAY_OF_MONTH, curDay);
            Cursor cEvents = calendarManager.getFromDbForDate(new Date(calendar.getTimeInMillis()));

            while (cEvents.moveToNext())
                events.add(new IntegratedCalendarEvent(cEvents));
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
                String dateString = android.text.format.DateUtils.formatDateTime(getApplicationContext(),
                        date.getTimeInMillis(), DateUtils.FORMAT_NUMERIC_DATE);

                return weekday.toUpperCase() + " " + dateString;
            }

            @Override
            public String interpretTime(int hour) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, 0);
                return android.text.format.DateUtils.formatDateTime(getApplicationContext(),
                        cal.getTimeInMillis(), android.text.format.DateUtils.FORMAT_SHOW_TIME);
            }
        });
    }
}
