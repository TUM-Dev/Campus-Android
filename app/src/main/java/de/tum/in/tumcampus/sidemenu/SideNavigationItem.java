package de.tum.in.tumcampus.sidemenu;

/**
 * Item of side navigation.
 * 
 * @author johnkil
 * 
 */
public class SideNavigationItem {

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
