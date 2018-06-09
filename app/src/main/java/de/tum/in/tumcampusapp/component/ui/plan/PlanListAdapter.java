package de.tum.in.tumcampusapp.component.ui.plan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;

/**
 * Adapter to show list of plans.
 */
public class PlanListAdapter extends BaseAdapter {

    private final List<PlanListEntry> planList;

    public class ViewHolder {
        public TextView detail;
        public ImageView icon;
        public TextView title;
    }

    public PlanListAdapter(List<PlanListEntry> planList) {
        this.planList = planList;
    }

    @Override
    public int getCount() {
        return planList.size();
    }

    @Override
    public Object getItem(int position) {
        return planList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.activity_plans_listview, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.list_menu_icon);
            holder.title = convertView.findViewById(R.id.list_menu_title);
            holder.detail = convertView.findViewById(R.id.list_menu_detail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Context context = parent.getContext();

        PlanListEntry item = planList.get(position);
        holder.icon.setImageResource(item.imageId);
        holder.title.setText(context.getResources()
                                    .getText(item.titleId));
        if (item.detailId == R.string.empty_string) {
            holder.detail.setVisibility(View.GONE);
        } else {
            holder.detail.setVisibility(View.VISIBLE);
            holder.detail.setText(context.getResources()
                                         .getText(item.detailId));
        }

        return convertView;
    }

    public static class PlanListEntry {
        public final int imageId;
        public final int titleId;
        public final int detailId;
        public final int imgId;

        public PlanListEntry(int thumbId, int titleId, int detailId, int imgId) {
            this.imageId = thumbId;
            this.titleId = titleId;
            this.detailId = detailId;
            this.imgId = imgId;
        }
    }
}