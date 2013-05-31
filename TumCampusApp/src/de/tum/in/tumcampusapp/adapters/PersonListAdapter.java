package de.tum.in.tumcampusapp.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.Employee;
import de.tum.in.tumcampusapp.models.Group;
import de.tum.in.tumcampusapp.models.Person;

/**
 * Custom UI adapter for a list of employees.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class PersonListAdapter extends BaseAdapter {
	static class ViewHolder {
		TextView tvDetails1;
		TextView tvDetails2;
		TextView tvName;
	}

	private static List<Person> employees;

	private final LayoutInflater mInflater;

	public PersonListAdapter(Context context, List<Person> results) {
		employees = results;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return employees.size();
	}

	@Override
	public Object getItem(int position) {
		return employees.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
			holder = new ViewHolder();
			holder.tvName = (TextView) convertView.findViewById(android.R.id.text1);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (employees.get(position) != null) {
			String infoText = "";

			Person p = employees.get(position);

			if (p != null && p instanceof Employee) {
				String title = ((Employee) p).getTitle();
				if (title != null) {
					infoText = title + " ";
				}
			}

			infoText += p.getName() + " " + p.getSurname();
			holder.tvName.setText(infoText);
		}
		return convertView;
	}
}
