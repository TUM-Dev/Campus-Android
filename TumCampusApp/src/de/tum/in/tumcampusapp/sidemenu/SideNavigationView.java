package de.tum.in.tumcampusapp.sidemenu;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;

/**
 * View of displaying side navigation.
 * 
 * @author e.shishkin
 * 
 */
public class SideNavigationView extends LinearLayout {
	private static final String LOG_TAG = SideNavigationView.class.getSimpleName();

	private LinearLayout navigationMenu;
	private ListView listView;
	private View outsideView;

	private ISideNavigationCallback callback;
	private ArrayList<SideNavigationItem> menuItems;
	private Mode mMode = Mode.LEFT;

	public static enum Mode {
		LEFT, RIGHT
	};

	/**
	 * Constructor of {@link SideNavigationView}.
	 * 
	 * @param context
	 */
	public SideNavigationView(Context context) {
		super(context);
		this.load();
	}

	/**
	 * Constructor of {@link SideNavigationView}.
	 * 
	 * @param context
	 * @param attrs
	 */
	public SideNavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.load();
	}

	/**
	 * Loading of side navigation view.
	 */
	private void load() {
		if (this.isInEditMode()) {
			return;
		}
		this.initView();
	}

	/**
	 * Initialization layout of side menu.
	 */
	private void initView() {
		this.removeAllViews();
		int sideNavigationRes;
		switch (this.mMode) {
		case LEFT:
			sideNavigationRes = R.layout.side_navigation_left;
			break;
		case RIGHT:
			sideNavigationRes = R.layout.side_navigation_right;
			break;

		default:
			sideNavigationRes = R.layout.side_navigation_left;
			break;
		}
		LayoutInflater.from(this.getContext()).inflate(sideNavigationRes, this, true);
		this.navigationMenu = (LinearLayout) this.findViewById(R.id.side_navigation_menu);
		this.listView = (ListView) this.findViewById(R.id.side_navigation_listview);
		this.outsideView = this.findViewById(R.id.side_navigation_outside_view);
		this.outsideView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SideNavigationView.this.hideMenu();
			}
		});
		this.listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (SideNavigationView.this.callback != null) {
					SideNavigationView.this.callback.onSideNavigationItemClick(SideNavigationView.this.menuItems.get(position));
				}
				SideNavigationView.this.hideMenu();
			}
		});
	}

	/**
	 * Setup of {@link ISideNavigationCallback} for callback of item click.
	 * 
	 * @param callback
	 */
	public void setMenuClickCallback(ISideNavigationCallback callback) {
		this.callback = callback;
	}

	/**
	 * Setup of side menu items.
	 * 
	 * @param menu
	 *            - resource ID
	 */
	public void setMenuItems(int menu) {
		this.parseXml(menu);
		if (this.menuItems != null && this.menuItems.size() > 0) {
			this.listView.setAdapter(new SideNavigationAdapter());
		}
	}

	/**
	 * Setup sliding mode of side menu ({@code Mode.LEFT} or {@code Mode.RIGHT}). {@code Mode.LEFT} by default.
	 * 
	 * @param mode
	 *            Sliding mode
	 */
	public void setMode(Mode mode) {
		if (this.isShown()) {
			this.hideMenu();
		}
		this.mMode = mode;
		this.initView();
		// setup menu items
		if (this.menuItems != null && this.menuItems.size() > 0) {
			this.listView.setAdapter(new SideNavigationAdapter());
		}
	}

	/**
	 * Getting current side menu mode ({@code Mode.LEFT} or {@code Mode.RIGHT}). {@code Mode.LEFT} by default.
	 * 
	 * @return side menu mode
	 */
	public Mode getMode() {
		return this.mMode;
	}

	public ArrayList<SideNavigationItem> getMenuItems() {
		return this.menuItems;
	}

	/**
	 * 
	 */
	@Override
	public void setBackgroundResource(int resource) {
		this.listView.setBackgroundResource(resource);
	}

	/**
	 * Show side navigation menu.
	 */
	public void showMenu() {
		this.outsideView.setVisibility(View.VISIBLE);
		this.outsideView.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.side_navigation_fade_in));
		// show navigation menu with animation
		int animRes;
		switch (this.mMode) {
		case LEFT:
			animRes = R.anim.side_navigation_in_from_left;
			break;
		case RIGHT:
			animRes = R.anim.side_navigation_in_from_right;
			break;

		default:
			animRes = R.anim.side_navigation_in_from_left;
			break;
		}
		this.navigationMenu.setVisibility(View.VISIBLE);
		this.navigationMenu.startAnimation(AnimationUtils.loadAnimation(this.getContext(), animRes));
	}

	/**
	 * Hide side navigation menu.
	 */
	public void hideMenu() {
		this.outsideView.setVisibility(View.GONE);
		this.outsideView.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.side_navigation_fade_out));
		// hide navigation menu with animation
		int animRes;
		switch (this.mMode) {
		case LEFT:
			animRes = R.anim.side_navigation_out_to_left;
			break;
		case RIGHT:
			animRes = R.anim.side_navigation_out_to_right;
			break;

		default:
			animRes = R.anim.side_navigation_out_to_left;
			break;
		}
		this.navigationMenu.setVisibility(View.GONE);
		this.navigationMenu.startAnimation(AnimationUtils.loadAnimation(this.getContext(), animRes));
	}

	/**
	 * Show/Hide side navigation menu depending on visibility.
	 */
	public void toggleMenu() {
		if (this.isShown()) {
			this.hideMenu();
		} else {
			this.showMenu();
		}
	}

	@Override
	public boolean isShown() {
		return this.navigationMenu.isShown();
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
			XmlResourceParser xrp = this.getResources().getXml(menu);
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
	 * @param text
	 * @return
	 */
	private String resourceIdToString(String resId) {
		if (!resId.contains("@")) {
			return resId;
		} else {
			String id = resId.replace("@", "");
			return this.getResources().getString(Integer.valueOf(id));
		}
	}

	private class SideNavigationAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public SideNavigationAdapter() {
			this.inflater = LayoutInflater.from(SideNavigationView.this.getContext());
		}

		@Override
		public int getCount() {
			return SideNavigationView.this.menuItems.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
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

			SideNavigationItem item = SideNavigationView.this.menuItems.get(position);
			holder.text.setText(SideNavigationView.this.menuItems.get(position).getText());

			// If item has an Icon its an entry, otherwise a seperator
			if (item.getIcon() != SideNavigationItem.DEFAULT_ICON_VALUE) {
				holder.icon.setVisibility(View.VISIBLE);
				holder.icon.setImageResource(SideNavigationView.this.menuItems.get(position).getIcon());
			} else {
				Log.d("icon", SideNavigationView.this.menuItems.get(position).getText() + " " + item.getIcon() + " " + SideNavigationItem.DEFAULT_ICON_VALUE);
				// Add some top padding and other background to indicate the sep
				LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.side_navigation_item_layout);
				lay.setPadding(lay.getPaddingLeft() - 1, lay.getPaddingTop() + 2, 0, lay.getPaddingBottom());
				lay.setBackgroundColor(SideNavigationView.this.getResources().getColor(R.color.side_navigation_list_divider_color));

				// Remove icon
				holder.icon.setVisibility(View.GONE);

				// Make not clickable
				convertView.setEnabled(false);
				convertView.setOnClickListener(null);
			}
			return convertView;
		}

		class ViewHolder {
			TextView text;
			ImageView icon;
		}

	}

}
