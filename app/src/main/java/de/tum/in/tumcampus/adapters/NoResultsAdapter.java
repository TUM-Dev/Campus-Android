package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import de.tum.in.tumcampus.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class NoResultsAdapter extends ArrayAdapter<String> implements StickyListHeadersAdapter {

	// constructor
	public NoResultsAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1,
                new String[]{context.getString(R.string.no_search_result)});
	}

    // Generate header view
    @Override
    public View getHeaderView(int pos, View convertView, ViewGroup parent) {
        return new View(getContext());
    }

    @Override
    public long getHeaderId(int i) {
        return 0;
    }
}
