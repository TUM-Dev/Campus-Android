package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.managers.CalendarManager;

public class LectureListSelectionAdapter extends CursorAdapter implements CompoundButton.OnCheckedChangeListener {
    private final int appWidgetId;
    private final CalendarManager calendarManager;

    public LectureListSelectionAdapter(Context context, Cursor c, boolean autoRequery, int appWidgetId) {
        super(context, c, autoRequery);
        this.appWidgetId = appWidgetId;
        this.calendarManager = new CalendarManager(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
                             .inflate(R.layout.list_timetable_configure_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckBox checkBox = view.findViewById(R.id.timetable_configure_item);
        checkBox.setChecked(cursor.getInt(cursor.getColumnIndex("is_on_blacklist")) == 0);
        checkBox.setText(cursor.getString(cursor.getColumnIndex("title")));
        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // save new preferences
        if (isChecked) {
            calendarManager.deleteLectureFromBlacklist(this.appWidgetId, (String) buttonView.getText());
        } else {
            calendarManager.addLectureToBlacklist(this.appWidgetId, (String) buttonView.getText());
        }
    }
}
