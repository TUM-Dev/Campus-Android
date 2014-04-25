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

	/**
	 * Validation searching via. text field
	 * 
	 * @param txtSearch
	 *            string to search for
	 */
	public void onSideNavigationSearch(String txtSearch);

}
