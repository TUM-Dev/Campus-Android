package de.tum.in.tumcampus.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.auxiliary.calendar.DayView;
import de.tum.in.tumcampus.auxiliary.calendar.EventLoader;
import de.tum.in.tumcampus.auxiliary.calendar.LoadEventsRequest;
import de.tum.in.tumcampus.fragments.DayFragment;
import de.tum.in.tumcampus.models.CalendarRowSet;
import de.tum.in.tumcampus.models.managers.CalendarManager;
import de.tum.in.tumcampus.models.managers.DatabaseManager;
import de.tum.in.tumcampus.models.managers.SyncManager;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
public class CalendarActivity extends ActivityForAccessingTumOnline<CalendarRowSet> implements OnClickListener {

    private static final int REQUEST_SYNC = 0;
    private static final int REQUEST_DELETE = 1;
    private static String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR};

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

    public CalendarActivity() {
        super(TUMOnlineConst.CALENDER, R.layout.activity_calendar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get time to show e.g. a lectures starting time or 0 for now
        Intent i = getIntent();
        if (i != null && i.hasExtra(EVENT_TIME)) {
            mEventTime = i.getLongExtra(EVENT_TIME, 0);
        }

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
            attachSectionPagerAdapter();
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
                attachSectionPagerAdapter();
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
                if (mWeekMode) {
                    menuItemSwitchView.setIcon(R.drawable.ic_action_day_view);
                } else {
                    menuItemSwitchView.setIcon(R.drawable.ic_action_week_view);
                }
                attachSectionPagerAdapter();
                return true;
            case R.id.action_export_calendar:
                detachSectionPagerAdapter();
                exportCalendarToGoogle();

                // Enable automatic calendar synchronisation
                Utils.setInternalSetting(this, Const.SYNC_CALENDAR, true);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_delete_calendar:
                deleteCalendarFromGoogle();
                return true;
            default:
                detachSectionPagerAdapter();
                isFetched = false;
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Link the Sections with the content with a section adapter. Additionally put the current date at the start position.
     */
    private void attachSectionPagerAdapter() {
        Time t = new Time();
        t.setToNow();
        DayView.mLeftBoundary = Time.getJulianDay(t.toMillis(true), t.gmtoff);
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, CalendarActivity.MONTH_AFTER);
        Date lastDate = calendar.getTime();
        int days = (int) ((lastDate.getTime() - now.getTime()) / DateUtils.DAY_IN_MILLIS);
        DayView.mRightBoundary = DayView.mLeftBoundary + days;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        LoadEventsRequest request = new LoadEventsRequest();
        ft.replace(R.id.fragment_container, new DayFragment(mEventTime, mWeekMode ? 7 : 1, new EventLoader(this, request)));
        ft.commitAllowingStateLoss();
    }

    /**
     * Detach the adapter form the Pager to make the async task not conflicting with the UI thread.
     */
    private void detachSectionPagerAdapter() {

        //mViewPager.setAdapter(null);
    }

    /**
     * Asynchronous task for exporting the calendar to a local Google calendar
     */
    void exportCalendarToGoogle() {
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
                            .setNegativeButton(CalendarActivity.this.getString(R.string.no), CalendarActivity.this).show();
                    attachSectionPagerAdapter();
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Display an AlertDialog with an explanation and a button to trigger the request.
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.permission_calendar_explanation))
                        .setPositiveButton(R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat
                                        .requestPermissions(CalendarActivity.this, PERMISSIONS_CALENDAR, id);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(CalendarActivity.this, PERMISSIONS_CALENDAR, id);
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Check if we got all Calendar permissions
        for(int result : grantResults) {
            if(result != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        //Rerun the interrupted action
        if(requestCode == REQUEST_SYNC) {
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
        //Check Calendar permission for Android 6.0
        if (!isPermissionGranted(REQUEST_DELETE)) {
            return;
        }
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
}
