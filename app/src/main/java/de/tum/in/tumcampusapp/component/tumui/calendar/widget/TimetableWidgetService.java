package de.tum.in.tumcampusapp.component.tumui.calendar.widget;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.tumui.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.utils.DateUtils;

@SuppressLint("Registered")
public class TimetableWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TimetableRemoteViewFactory(this.getApplicationContext(), intent);
    }

    private class TimetableRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Context applicationContext;
        private final int appWidgetID;
        private List<IntegratedCalendarEvent> calendarEvents;

        TimetableRemoteViewFactory(Context applicationContext, Intent intent) {
            this.applicationContext = applicationContext.getApplicationContext();
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            calendarEvents = new ArrayList<>();
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            // get events
            CalendarController calendarController = new CalendarController(this.applicationContext);
            calendarEvents = calendarController.getNextDaysFromDb(14, this.appWidgetID);

            // set isFirstOnDay flags
            Calendar currentDate = Calendar.getInstance();
            Date startDate = new Date();
            startDate.setTime(0);
            currentDate.setTime(startDate);
            for (IntegratedCalendarEvent calendarEvent : calendarEvents) {
                Calendar calendarDate = calendarEvent.getStartTime();
                if (!DateUtils.isSameDay(currentDate, calendarDate)) {
                    currentDate = calendarDate;
                    calendarEvent.setIsFirstOnDay(true);
                }
            }
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return this.calendarEvents.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.timetable_widget_item);
            if (this.calendarEvents == null) {
                return rv;
            }

            // get the lecture for this view
            IntegratedCalendarEvent currentItem = this.calendarEvents.get(position);
            if (currentItem == null) {
                return null;
            }

            Calendar calendar = currentItem.getStartTime();

            // Setup the date
            if (currentItem.isFirstOnDay()) {
                rv.setTextViewText(R.id.timetable_widget_date_day, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                rv.setTextViewText(R.id.timetable_widget_date_weekday, calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rv.setViewPadding(R.id.timetable_widget_item, 0, 15, 0, 0);
                }
            } else {
                // overwrite unused parameters, as the elements are reused they may could be filled with old parameters
                rv.setTextViewText(R.id.timetable_widget_date_day, "");
                rv.setTextViewText(R.id.timetable_widget_date_weekday, "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rv.setViewPadding(R.id.timetable_widget_item, 0, 0, 0, 0);
                }
            }
            // TODO add month labels every new month

            // Setup event color
            rv.setInt(R.id.timetable_widget_event, "setBackgroundColor", currentItem.getColor());

            // Setup event title
            rv.setTextViewText(R.id.timetable_widget_event_title, currentItem.getName());

            // Setup event time
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext);
            String time = timeFormat.format(currentItem.getStartTime()
                                                       .getTime());
            time += "-" + timeFormat.format(currentItem.getEndTime()
                                                       .getTime());
            rv.setTextViewText(R.id.timetable_widget_event_time, time);

            // Setup event location
            rv.setTextViewText(R.id.timetable_widget_event_location, currentItem.getLocation());

            // Setup action to open roomFinder
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(SearchManager.QUERY, currentItem.getLocation());
            rv.setOnClickFillInIntent(R.id.timetable_widget_event, fillInIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
