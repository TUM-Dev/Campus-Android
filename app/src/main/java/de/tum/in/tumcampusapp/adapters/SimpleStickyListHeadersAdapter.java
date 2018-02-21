package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * An abstract StickyListHeadersAdapter helps to reduce redundant work for using StickyListHeaders.
 * It implements some method requred by StickyListHeadersAdapter, including getHeaderView getHeaderId,
 * getCount, getItem and getItemId.
 * By extending this class, only getView need to implement.
 * On the other hand this class requires the data model implementing its interface to get header name and id.
 *
 * @param <T> The model of data
 */
abstract public class SimpleStickyListHeadersAdapter<T extends SimpleStickyListHeadersAdapter.SimpleStickyListItem>
        extends BaseAdapter implements StickyListHeadersAdapter {
    List<T> infoList;
    Context context;
    private final List<String> filters;
    final LayoutInflater mInflater;

    SimpleStickyListHeadersAdapter(Context context, List<T> infos) {
        this.context = context;
        this.infoList = infos;
        this.mInflater = LayoutInflater.from(context);

        filters = new ArrayList<>();
        for (T result : infoList) {
            String item = result.getHeaderId();
            if (!filters.contains(item)) {
                filters.add(item);
            }
        }
    }

    // getView mtehod need to be implemented by subclass
    @Override
    abstract public View getView(int position, View convertView, ViewGroup parent);

    public List<T> getInfoList() {
        return infoList;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    /**
     * Genernate header for this item.
     * This methoded can be override if header name need to be modified.
     *
     * @param item the item
     * @return the header for this item
     */
    String genenrateHeaderName(T item) {
        return item.getHeadName();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup partent) {
        HeaderViewHolder holder;
        View view = convertView;

        if (view == null) {
            holder = new HeaderViewHolder();
            view = mInflater.inflate(R.layout.header, partent, false);
            holder.text = view.findViewById(R.id.lecture_header);
            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) view.getTag();
        }

        String headerText = genenrateHeaderName(infoList.get(position));
        holder.text.setText(headerText);

        return view;
    }

    @Override
    public long getHeaderId(int i) {
        return filters.indexOf(infoList.get(i)
                                       .getHeaderId());
    }

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public Object getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Header view
    private static class HeaderViewHolder {
        TextView text;
    }

    public interface SimpleStickyListItem {
        String getHeadName();

        String getHeaderId();
    }
}