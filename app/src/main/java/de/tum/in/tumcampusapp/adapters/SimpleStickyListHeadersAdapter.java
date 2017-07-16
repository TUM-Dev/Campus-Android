package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

abstract public class SimpleStickyListHeadersAdapter<T extends SimpleStickyListHeadersAdapter.SimpleStickyListItem>
        extends BaseAdapter implements StickyListHeadersAdapter {
    List<T> infoList;
    Context context;
    final LayoutInflater inflater;

    public SimpleStickyListHeadersAdapter(Context context, List<T> infos){
        this.context = context;
        this.infoList = infos;
        this.inflater = LayoutInflater.from(context);
    }

    public List<T> getInfoList(){
        return infoList;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup partent) {
        HeaderViewHolder holder;
        View view = convertView;

        if (view == null) {
            holder = new HeaderViewHolder();
            view = inflater.inflate(R.layout.header, partent, false);
            holder.text = (TextView) view.findViewById(R.id.lecture_header);
            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) view.getTag();
        }

        String headerText = infoList.get(position).getHeadName();
        holder.text.setText(headerText);

        return view;
    }

    @Override
    public long getHeaderId(int i) {
        String headName = infoList.get(i).getHeadName();
        if(headName == null || headName.equals("null")){
            return 'Z';
        }
        return headName.hashCode();
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
        public String getHeadName();
    }
}
