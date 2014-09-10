package de.tum.in.tumcampus.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ListMenuEntry;

/**
 * Adapter to combine own layouts with the list view.
 * 
 * @author Sascha
 * 
 */
public class StartListAdapter extends BaseAdapter {

	public static class ViewHolder {
		public TextView detail;
		public ImageView icon;
		public TextView title;
	}

	private Activity activity;
	private LayoutInflater inflater = null;
	private int layoutId;
	private ArrayList<ListMenuEntry> listMenuEntrySet;

    public StartListAdapter(Activity activity, int layoutId, ArrayList<ListMenuEntry> listMenuEntrySet) {
		this.activity = activity;
		this.layoutId = layoutId;
		this.listMenuEntrySet = listMenuEntrySet;
		this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return listMenuEntrySet.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;

		ViewHolder holder;
		if (convertView == null) {
			vi = this.inflater.inflate(this.layoutId, null);

			holder = new ViewHolder();
			holder.icon = (ImageView) vi.findViewById(R.id.list_menu_icon);
			holder.title = (TextView) vi.findViewById(R.id.list_menu_title);
			holder.detail = (TextView) vi.findViewById(R.id.list_menu_detail);
			vi.setTag(holder);
		} else {
			holder = (ViewHolder) vi.getTag();
		}

        ListMenuEntry item = listMenuEntrySet.get(position);
		holder.icon.setImageResource(item.imageId);
		holder.title.setText(activity.getResources().getText(item.titleId));
        if(item.detailId==R.string.empty_string) {
            holder.detail.setVisibility(View.GONE);
        } else {
            holder.detail.setVisibility(View.VISIBLE);
            holder.detail.setText(activity.getResources().getText(item.detailId));
        }
		return vi;
	}
}