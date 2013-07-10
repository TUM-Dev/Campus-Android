package de.tum.in.tumcampusapp.auxiliary;

public interface SearchResultListener {
	public void onSearchResults(String[] result);

	public void onSearchError(String message);
}
