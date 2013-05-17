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
	private static List<Person> employees;

	private final LayoutInflater mInflater;

	public PersonListAdapter(Context context, List<Person> results) {
		employees = results;
		mInflater = LayoutInflater.from(context);
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
			convertView = mInflater.inflate(R.layout.activity_persons_listview, null);
			holder = new ViewHolder();
			holder.tvName = (TextView) convertView.findViewById(R.id.name);
			holder.tvDetails1 = (TextView) convertView.findViewById(R.id.tv1);
			holder.tvDetails2 = (TextView) convertView.findViewById(R.id.tv2);

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

			if (p instanceof Employee) {
				Employee e = ((Employee) p);
				List<Group> groups = e.getGroups();
				if (groups != null && groups.size() > 0) {
					holder.tvDetails1.setText(groups.get(0).getOrg() + " (" + groups.get(0).getId() + ")");
					holder.tvDetails2.setText("(" + groups.get(0).getTitle() + ")");
				} else {
					holder.tvDetails1.setText("");
					holder.tvDetails2.setText("");
				}
			}

		}

		return convertView;
	}

	static class ViewHolder {
		TextView tvName;
		TextView tvDetails1;
		TextView tvDetails2;
	}
}
