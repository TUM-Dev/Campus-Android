package de.tum.in.tumcampusapp.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CalendarManager;
import de.tum.in.tumcampusapp.models.tumo.CalendarRow;

@SuppressLint("Registered")
public class TimetableWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TimetableRemoteViewFactory(this.getApplicationContext(), intent);
    }

    private class TimetableRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Context applicationContext;
        private int appWidgetID;
        private List<CalendarRow> calendarRowList;

        TimetableRemoteViewFactory(Context applicationContext, Intent intent) {
            this.applicationContext = applicationContext.getApplicationContext();
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            calendarRowList = new ArrayList<>();
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            CalendarManager calendarManager = new CalendarManager(this.applicationContext);
            calendarRowList = calendarManager.getNextDaysFromDb(14);
            Date currentDate = new Date();
            currentDate.setTime(0);
            for(CalendarRow calendarRow : calendarRowList){
                Date calendarDate = calendarRow.getDtstartDate();
                if(!Utils.isSameDay(currentDate, calendarDate)){
                    currentDate = calendarDate;
                    calendarRow.setIsFirstOnDay(true);
                }
            }
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return this.calendarRowList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(applicationContext.getPackageName(), R.layout.timetable_widget_item);
            if (this.calendarRowList == null) {
                return rv;
            }

            // get the lecture for this view
            CalendarRow currentItem = this.calendarRowList.get(position);
            if (currentItem == null) {
                return null;
            }

            // Setup the line symbol
            rv.setTextViewText(R.id.timetable_widget_title, currentItem.getTitle());

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