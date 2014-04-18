package de.tum.in.tumcampusapp.sidemenu;

/**
 * Callback interface for {@link SideNavigationView}.
 * 
 * @author e.shishkin
 * 
 */
public interface ISideNavigationCallback {

	/**
	 * Validation clicking on side navigation item.
	 * 
	 * @param sideNavigationItem
	 *            id of selected item
	 */
	public void onSideNavigationItemClick(SideNavigationItem sideNavigationItem);

}
