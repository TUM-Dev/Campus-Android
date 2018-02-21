package de.tum.in.tumcampusapp.component.other.generic.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import de.tum.in.tumcampusapp.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This adapter class produces just one single item saying now search results
 */
public class NoResultsAdapter extends ArrayAdapter<String> implements StickyListHeadersAdapter {

    // constructor
    public NoResultsAdapter(Context context) {
        super(context, R.layout.listview_simple_item_center, new String[]{context.getString(R.string.no_search_result)});
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

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
