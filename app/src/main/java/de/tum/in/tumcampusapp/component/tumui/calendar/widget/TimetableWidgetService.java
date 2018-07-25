package de.tum.in.tumcampusapp.component.tumui.calendar.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.tumui.calendar.IntegratedCalendarEvent;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

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
            this.applicationContext = applicationContext;
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            calendarEvents = new ArrayList<>();
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            // Get events
            CalendarController calendarController = new CalendarController(this.applicationContext);
            calendarEvents = calendarController.getNextDaysFromDb(14, this.appWidgetID);

            // Set isFirstOnDay flags
            if (!calendarEvents.isEmpty()) {
                calendarEvents.get(0).setIsFirstOnDay(true);
            }

            for (int i = 1; i < calendarEvents.size(); i++) {
                IntegratedCalendarEvent lastEvent = calendarEvents.get(i - 1);
                DateTime lastTime = new DateTime(lastEvent.getStartTime()
                                                          .getTimeInMillis());

                IntegratedCalendarEvent thisEvent = calendarEvents.get(i);
                DateTime thisTime = new DateTime(thisEvent.getStartTime()
                                                          .getTimeInMillis());

                if (!DateTimeUtils.INSTANCE.isSameDay(lastTime, thisTime)) {
                    thisEvent.setIsFirstOnDay(true);
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

            // Get the lecture for this view
            IntegratedCalendarEvent currentItem = this.calendarEvents.get(position);
            if (currentItem == null) {
                return null;
            }

            DateTime startTime = new DateTime(currentItem.getStartTime().getTimeInMillis());

            // Setup the date
            if (currentItem.isFirstOnDay()) {
                rv.setTextViewText(R.id.timetable_widget_date_day, String.valueOf(startTime.getDayOfMonth()));
                rv.setTextViewText(R.id.timetable_widget_date_weekday, startTime.dayOfWeek()
                                                                                .getAsShortText(Locale.getDefault()));
                rv.setViewPadding(R.id.timetable_widget_item, 0, 15, 0, 0);
            } else {
                // Overwrite unused parameters, as the elements are reused they may could be filled with old parameters
                rv.setTextViewText(R.id.timetable_widget_date_day, "");
                rv.setTextViewText(R.id.timetable_widget_date_weekday, "");
                rv.setViewPadding(R.id.timetable_widget_item, 0, 0, 0, 0);
            }

            // TODO: Display month label if event is the first event in a new month

            // Setup event color
            rv.setInt(R.id.timetable_widget_event, "setBackgroundColor", currentItem.getColor());

            // Setup event title
            rv.setTextViewText(R.id.timetable_widget_event_title, currentItem.getName());

            // Setup event time
            DateTimeFormatter formatter = DateTimeFormat.shortTime();

            String startTimeText = formatter.print(startTime);
            DateTime endTime = new DateTime(currentItem.getEndTime().getTimeInMillis());
            String endTimeText = formatter.print(endTime);

            String eventTime =
                    getString(R.string.event_start_end_format_string, startTimeText, endTimeText);
            rv.setTextViewText(R.id.timetable_widget_event_time, eventTime);

            // Setup event location
            rv.setTextViewText(R.id.timetable_widget_event_location, currentItem.getLocation());

            // Setup action to open calendar
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Const.EVENT_TIME, currentItem.getStartTime().getTimeInMillis());
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
