package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.CafeteriaActivity;
import de.tum.in.tumcampus.activities.CalendarActivity;
import de.tum.in.tumcampus.activities.ChatRoomsSearchActivity;
import de.tum.in.tumcampus.activities.CurriculaActivity;
import de.tum.in.tumcampus.activities.FeedsActivity;
import de.tum.in.tumcampus.activities.GalleryActivity;
import de.tum.in.tumcampus.activities.GradesActivity;
import de.tum.in.tumcampus.activities.LecturesPersonalActivity;
import de.tum.in.tumcampus.activities.NewsActivity;
import de.tum.in.tumcampus.activities.OpeningHoursListActivity;
import de.tum.in.tumcampus.activities.OrganisationActivity;
import de.tum.in.tumcampus.activities.PersonsSearchActivity;
import de.tum.in.tumcampus.activities.PlansActivity;
import de.tum.in.tumcampus.activities.RoomfinderActivity;
import de.tum.in.tumcampus.activities.TransportationActivity;
import de.tum.in.tumcampus.activities.TuitionFeesActivity;
import de.tum.in.tumcampus.activities.UserPreferencesActivity;

public class SideNavigationAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;

    public SideNavigationAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return menuItems.length;
    }

    @Override
    public Object getItem(int position) {
        return menuItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.side_navigation_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.side_navigation_item_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.side_navigation_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SideNavigationItem item = menuItems[position];
        holder.text.setText(menuItems[position].getText(mContext));

        LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.side_navigation_item_layout);

        // If item has an Icon its an entry
        if (item.getIcon() != SideNavigationItem.NO_ICON_VALUE) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageResource(menuItems[position].getIcon());
            lay.setBackgroundColor(mContext.getResources().getColor(R.color.side_navigation_background));
        } else {
            // Check if it has an activity - if not, its a seperator
            if (item.getActivity() == null) {
                // Add some top padding and other background to indicate the sep
                lay.setBackgroundColor(mContext.getResources().getColor(R.color.side_navigation_list_divider_color));

                int paddingTop = (int) mContext.getResources().getDimension(R.dimen.side_navigation_item_padding_topbottom) + 4;
                int paddingLeft = (int) mContext.getResources().getDimension(R.dimen.side_navigation_item_padding_leftright) - 2;
                lay.setPadding(paddingLeft, paddingTop, 0, lay.getPaddingBottom());

                // Make not clickable
                convertView.setEnabled(false);
                convertView.setOnClickListener(null);
            }

            // Remove icon
            holder.icon.setVisibility(View.GONE);

        }
        return convertView;
    }

    class ViewHolder {
        TextView text;
        ImageView icon;
    }

    public static class SideNavigationItem {
        public static final int NO_ICON_VALUE = -1;

        private final int textRes;
        private final int icon;
        private final Class<?> activity;

        public SideNavigationItem(int text) {
            icon = NO_ICON_VALUE;
            activity = null;
            textRes = text;
        }

        public SideNavigationItem(int text, int sym, Class<?> activ) {
            textRes = text;
            icon = sym;
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

    }

    private static final SideNavigationItem[] menuItems = {
            new SideNavigationItem(R.string.my_tum),
            new SideNavigationItem(R.string.schedule,R.drawable.calendar, CalendarActivity.class),
            new SideNavigationItem(R.string.my_lectures,R.drawable.calculator, LecturesPersonalActivity.class),
            new SideNavigationItem(R.string.my_grades,R.drawable.chart, GradesActivity.class),
            new SideNavigationItem(R.string.tuition_fees,R.drawable.finance, TuitionFeesActivity.class),
            new SideNavigationItem(R.string.tum_common),
            new SideNavigationItem(R.string.menues,R.drawable.shopping_cart, CafeteriaActivity.class),
            new SideNavigationItem(R.string.rss_feeds,R.drawable.fax, FeedsActivity.class),
            new SideNavigationItem(R.string.study_plans,R.drawable.documents, CurriculaActivity.class),
            //new SideNavigationItem(R.string.events,R.drawable.camera, EventsActivity.class),
            //new SideNavigationItem(R.string.gallery,R.drawable.pictures, GalleryActivity.class),
            new SideNavigationItem(R.string.person_search,R.drawable.users, PersonsSearchActivity.class),
            new SideNavigationItem(R.string.plans,R.drawable.web, PlansActivity.class),
            new SideNavigationItem(R.string.roomfinder,R.drawable.home, RoomfinderActivity.class),
            new SideNavigationItem(R.string.opening_hours,R.drawable.unlock, OpeningHoursListActivity.class),
            new SideNavigationItem(R.string.organisations,R.drawable.chat, OrganisationActivity.class),
            new SideNavigationItem(R.string.mvv,R.drawable.show_info, TransportationActivity.class),
            new SideNavigationItem(R.string.chat_rooms, R.drawable.chat, ChatRoomsSearchActivity.class),
            new SideNavigationItem(R.string.tum_news,R.drawable.mail, NewsActivity.class),
    };

}