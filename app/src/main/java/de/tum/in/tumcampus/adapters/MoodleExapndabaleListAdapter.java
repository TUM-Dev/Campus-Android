package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Created by a2k on 6/7/2015.
 * This class handles the view output of my courses page in moodle activity
 */

public class MoodleExapndabaleListAdapter  extends BaseExpandableListAdapter{

    private Context context;
    private List<String> listDataheaders;

    /* Just the first element of this
    * list of string is filled with course description
    the other element are empty
    */
    private Map<String, List<String>> dataChild;

    public MoodleExapndabaleListAdapter(Context context, List<String> headers, Map<String, List<String>> child ){
        this.context = context;
        this.listDataheaders = headers;
        this.dataChild = child;
    }

    @Override
    public int getGroupCount() {
        return listDataheaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        return dataChild.get(listDataheaders.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataheaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dataChild.get(listDataheaders.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.moodle_list_header, null);
        }

        TextView header = (TextView) convertView.findViewById(R.id.moodleListHeader);
        header.setText(headerTitle);
        header.setTypeface(null, Typeface.BOLD);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childText = (String)getChild(groupPosition, childPosition);

        if (childText.equals("") || childText.equals(null))
            childText = "No description available";

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.moodle_list_item, null);
        }

        TextView description = (TextView)convertView.findViewById(R.id.moodleListItem);
        description.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
