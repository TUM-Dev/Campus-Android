package de.tum.in.tumcampusapp.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampusapp.models.OrgItem;

/**
 * Adapterclass for {@OrgItemList}. The purpose is to get Items
 * from the list and set them into the Textview
 * 
 * @author Thomas Behrens
 * @review Vincez Doelle, Daniel G. Mayr
 */

public class OrgItemListAdapter extends BaseAdapter {

	static class ViewHolder {
		TextView tvMainField;
		TextView tvSubField1;
		TextView tvSubField2;
	}

	/**
	 * Organisation list, that is getting shown
	 */
	private static List<OrgItem> organisationList;

	/**
	 * To inflate the layout
	 */
	private final LayoutInflater mInflater;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		// set up TextView elements if given TextView != null
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.activity_organisation_listview, null);
			holder = new ViewHolder();
			holder.tvMainField = (TextView) convertView.findViewById(R.id.name);
			holder.tvSubField1 = (TextView) convertView.findViewById(R.id.tv1);
			holder.tvSubField2 = (TextView) convertView.findViewById(R.id.tv2);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// get the name depending on the own language
		if (organisationList.get(position) != null) {
			if (System.getProperty("user.language") == "de") {
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
