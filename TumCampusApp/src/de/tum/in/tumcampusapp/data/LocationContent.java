package de.tum.in.tumcampusapp.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class LocationContent {

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class Location {
		public String content;
		public String id;

		public Location(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	@SuppressLint("UseSparseArrays")
	public static Map<String, Location> ITEM_MAP = new HashMap<String, Location>();

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<Location> ITEMS = new ArrayList<Location>();

	static {
		// addItem(new Location("1", "Item 1"));
	}

	public static void addItem(Location item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	public static void clear() {
		ITEMS.clear();
		ITEM_MAP.clear();
	}
}
