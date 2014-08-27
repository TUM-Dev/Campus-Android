package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaActivity;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.activities.ChatRoomsSearchActivity;
import de.tum.in.tumcampus.activities.CurriculaActivity;
import de.tum.in.tumcampus.activities.FeedsActivity;
import de.tum.in.tumcampus.activities.GradesActivity;
import de.tum.in.tumcampus.activities.LecturesPersonalActivity;
import de.tum.in.tumcampus.activities.NewsActivity;
import de.tum.in.tumcampus.activities.OpeningHoursListActivity;
import de.tum.in.tumcampus.activities.OrganisationActivity;
import de.tum.in.tumcampus.activities.PersonsSearchActivity;
import de.tum.in.tumcampus.activities.PlansActivity;
import de.tum.in.tumcampus.activities.RoomFinderActivity;
import de.tum.in.tumcampus.activities.TransportationActivity;
import de.tum.in.tumcampus.activities.TuitionFeesActivity;
import de.tum.in.tumcampus.auxiliary.AccessTokenManager;

public class SideNavigationAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private final ArrayList<SideNavigationItem> mVisibleMenuItems;

    public SideNavigationAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        boolean mHasTUMOAccess = new AccessTokenManager(context).hasValidAccessToken();
        mVisibleMenuItems = new ArrayList<SideNavigationItem>();
        for(SideNavigationItem item : menuItems) {
            if(!mHasTUMOAccess && item.needsTUMO)
                continue;
            mVisibleMenuItems.add(item);
        }
    }

    @Override
    public int getCount() {
        return mVisibleMenuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mVisibleMenuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SideNavigationItem item = mVisibleMenuItems.get(position);
        ViewHolder holder = null;
        View view = convertView;

        if (view == null) {
            int layout = R.layout.side_navigation_item;
            if (item.isHeader())
                layout = R.layout.side_navigation_header;

            view = mInflater.inflate(layout, null);

            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.side_navigation_text);
            holder.icon = (ImageView) view.findViewById(R.id.side_navigation_icon);
            view.setTag(holder);
        }

        if (holder == null && view != null) {
            Object tag = view.getTag();
            if (tag instanceof ViewHolder) {
                holder = (ViewHolder) tag;
            }
        }


        if(item != null && holder != null) {
            holder.text.setText(item.getText(mContext));

            if (holder.icon != null && item.getIcon()!=SideNavigationItem.NO_ICON_VALUE) {
                holder.icon.setImageResource(item.getIcon());
            }
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mVisibleMenuItems.get(position).isHeader() ? 0 : 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return !mVisibleMenuItems.get(position).isHeader();
    }

    class ViewHolder {
        TextView text;
        ImageView icon;
    }

    public static class SideNavigationItem {
        public static final int NO_ICON_VALUE = -1;

        private final int textRes;
        private final int icon;
        private final boolean needsTUMO;
        private final Class<?> activity;

        public SideNavigationItem(int text, boolean tumo) {
            icon = NO_ICON_VALUE;
            activity = null;
            needsTUMO = tumo;
            textRes = text;
        }

        public SideNavigationItem(int text, int sym, boolean tumo, Class<?> activ) {
            textRes = text;
            icon = sym;
            needsTUMO = tumo;
            activity = activ;
        }

        public String getText(Context c) {
            return c.getText(textRes).toString();
        }

        public int getIcon() {
            return this.icon;
        }

        public Class<?> getActivity() {
            return this.activity;
        }

        public boolean isHeader() {
            return icon==NO_ICON_VALUE;
        }

    }

    private static final SideNavigationItem[] menuItems = {
            new SideNavigationItem(R.string.my_tum, true),
            new SideNavigationItem(R.string.schedule,R.drawable.ic_calendar, true, CalendarActivity.class),
            new SideNavigationItem(R.string.my_lectures,R.drawable.ic_my_lectures, true, LecturesPersonalActivity.class),
            new SideNavigationItem(R.string.my_grades,R.drawable.ic_my_grades, true, GradesActivity.class),
            new SideNavigationItem(R.string.chat_rooms, R.drawable.ic_comment, true, ChatRoomsSearchActivity.class),
            new SideNavigationItem(R.string.tuition_fees,R.drawable.ic_money, true, TuitionFeesActivity.class),
            new SideNavigationItem(R.string.tum_common, false),
            new SideNavigationItem(R.string.menues,R.drawable.ic_cutlery, false, CafeteriaActivity.class),
            new SideNavigationItem(R.string.rss_feeds,R.drawable.ic_rss, false, FeedsActivity.class),
            new SideNavigationItem(R.string.study_plans,R.drawable.ic_study_plans, false, CurriculaActivity.class),
            new SideNavigationItem(R.string.person_search,R.drawable.ic_users, true, PersonsSearchActivity.class),
            new SideNavigationItem(R.string.plans,R.drawable.ic_plans, false, PlansActivity.class),
            new SideNavigationItem(R.string.roomfinder,R.drawable.ic_place, false, RoomFinderActivity.class),
            new SideNavigationItem(R.string.opening_hours,R.drawable.ic_time, false, OpeningHoursListActivity.class),
            new SideNavigationItem(R.string.organisations,R.drawable.ic_organisations, true, OrganisationActivity.class),
            new SideNavigationItem(R.string.mvv,R.drawable.ic_mvv, false, TransportationActivity.class),
            new SideNavigationItem(R.string.tum_news,R.drawable.ic_rss, false, NewsActivity.class),
    };

}