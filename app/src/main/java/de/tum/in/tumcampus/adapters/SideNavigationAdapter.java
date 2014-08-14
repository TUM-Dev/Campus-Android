package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

import de.tum.in.tumcampus.R;

public class SideNavigationAdapter extends BaseAdapter {
    private static final String LOG_TAG = "sideAdapter";
    private LayoutInflater inflater;
    private ArrayList<SideNavigationItem> menuItems;
    private Context mContext;

    public SideNavigationAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        mContext = context;
        parseXml(R.menu.menu_side);
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.side_navigation_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.side_navigation_item_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.side_navigation_item_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SideNavigationItem item = menuItems.get(position);
        holder.text.setText(menuItems.get(position).getText());

        // If item has an Icon its an entry
        if (item.getIcon() != SideNavigationItem.DEFAULT_ICON_VALUE) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageResource(menuItems.get(position).getIcon());
        } else {
            // Check if it has an activity - if not, its a seperator
            if (item.getActivity() == null) {
                Log.d("icon", menuItems.get(position).getText() + " " + item.getIcon() + " "
                        + SideNavigationItem.DEFAULT_ICON_VALUE);
                // Add some top padding and other background to indicate the sep
                LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.side_navigation_item_layout);
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


    /**
     * Parse XML describe menu.
     *
     * @param menu
     *            - resource ID
     */
    private void parseXml(int menu) {
        this.menuItems = new ArrayList<SideNavigationItem>();
        try {
            XmlResourceParser xrp = mContext.getResources().getXml(menu);
            xrp.next();
            int eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String elemName = xrp.getName();
                    if (elemName.equals("item")) {
                        String textId = xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "title");
                        String iconId = xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "icon");
                        String resId = xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "id");
                        String activity = xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "titleCondensed");
                        String enabled = xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "enabled");

                        if (enabled != "false") {
                            SideNavigationItem item = new SideNavigationItem();
                            item.setId(Integer.valueOf(resId.replace("@", "")));
                            item.setText(this.resourceIdToString(textId));
                            item.setActivity(activity);

                            if (iconId != null) {
                                try {
                                    item.setIcon(Integer.valueOf(iconId.replace("@", "")));
                                } catch (NumberFormatException e) {
                                    Log.d(LOG_TAG, "Item " + item.getId() + " not have icon");
                                }
                            }
                            this.menuItems.add(item);
                        }
                    }
                }
                eventType = xrp.next();
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, e);
        }
    }

    /**
     * Convert resource ID to String.
     *
     * @param resId
     * @return
     */
    private String resourceIdToString(String resId) {
        if (!resId.contains("@")) {
            return resId;
        } else {
            String id = resId.replace("@", "");
            return mContext.getResources().getString(Integer.valueOf(id));
        }
    }

    public static class SideNavigationItem {

        public static int DEFAULT_ICON_VALUE = -1;

        private int id;
        private String text;
        private int icon = DEFAULT_ICON_VALUE;
        private String activity;

        public int getId() {
            return this.id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return this.text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getIcon() {
            return this.icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }

        public String getActivity() {
            return this.activity;
        }

    }

}