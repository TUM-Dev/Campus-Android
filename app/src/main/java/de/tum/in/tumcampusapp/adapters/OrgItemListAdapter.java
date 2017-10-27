package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.tumo.OrgItem;

/**
 * Adapter class for {@link OrgItem} list. The purpose is to get Items
 * from the list and set them into the TextView
 */
public class OrgItemListAdapter extends BaseAdapter {

    /**
     * Organisation list, that is getting shown
     */
    private final List<OrgItem> organisationList;

    /**
     * To inflate the layout
     */
    private final LayoutInflater mInflater;

    static class ViewHolder {
        TextView tvMainField;
        TextView tvSubField1;
    }

    public OrgItemListAdapter(Context context, List<OrgItem> results) {
        organisationList = results;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * get number of list items
     */
    @Override
    public int getCount() {
        return organisationList.size();
    }

    /**
     * get position in the list
     */
    @Override
    public Object getItem(int position) {
        return organisationList.get(position);
    }

    /**
     * get id of the item in the list
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * get a view in which the TextView elements have the values
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        View convertView = view;
        // set up TextView elements if given TextView != null
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_organisation_listview, parent, false);

            holder = new ViewHolder();
            holder.tvMainField = convertView.findViewById(R.id.name);
            holder.tvSubField1 = convertView.findViewById(R.id.tv1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // get the name depending on the own language
        if (organisationList.get(position) != null) {
            if (System.getProperty("user.language")
                      .equals("de")) {
                holder.tvMainField.setText(organisationList.get(position)
                                                           .getNameDe());
                holder.tvSubField1.setText(organisationList.get(position)
                                                           .getNameEn());
            } else {
                holder.tvMainField.setText(organisationList.get(position)
                                                           .getNameEn());
                holder.tvSubField1.setText(organisationList.get(position)
                                                           .getNameDe());
            }
        }

        return convertView;
    }
}
