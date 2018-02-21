package de.tum.in.tumcampusapp.component.tumui.lectures.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.utils.Utils;

public class LectureListSelectionAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {
    private final int appWidgetId;
    private final CalendarController calendarController;
    private final List<CalendarItem> calendarItems;
    private final LayoutInflater mInflater;

    public LectureListSelectionAdapter(Context context, List<CalendarItem> cItems, int appWidgetId) {
        calendarItems = cItems;
        this.appWidgetId = appWidgetId;
        this.calendarController = new CalendarController(context);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // save new preferences
        Utils.logv("Widget asked to change " + buttonView.getText()
                                                         .toString() + " to " + isChecked);
        if (isChecked) {
            calendarController.deleteLectureFromBlacklist(this.appWidgetId, (String) buttonView.getText());
        } else {
            calendarController.addLectureToBlacklist(this.appWidgetId, (String) buttonView.getText());
        }
    }

    @Override
    public int getCount() {
        return calendarItems.size();
    }

    @Override
    public Object getItem(int position) {
        return calendarItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(calendarItems.get(position)
                                           .getNr());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_timetable_configure_item, parent, false);
        }
        CheckBox checkBox = view.findViewById(R.id.timetable_configure_item);
        checkBox.setChecked(!calendarItems.get(position)
                                          .getBlacklisted());
        checkBox.setText(calendarItems.get(position)
                                      .getTitle());
        checkBox.setOnCheckedChangeListener(this);

        return view;
    }
}
