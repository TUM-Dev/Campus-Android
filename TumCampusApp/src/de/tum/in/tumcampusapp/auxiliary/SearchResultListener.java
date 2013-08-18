package de.tum.in.tumcampusapp.auxiliary;

/**
 * SearchResultListener
 * 
 * @author
 */
public interface SearchResultListener {
	public void onSearchError(String message);

	public void onSearchResults(String[] result);
}
