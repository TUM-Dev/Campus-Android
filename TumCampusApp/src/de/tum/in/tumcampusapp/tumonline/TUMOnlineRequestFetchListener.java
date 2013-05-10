package de.tum.in.tumcampusapp.tumonline;

/**
 * this interface should frame, how to implement a listener for the
 * fetchInteractive method in TUMOnlineRequest
 * 
 * @author Daniel G. Mayr
 */
public interface TUMOnlineRequestFetchListener {

	public void onCommonError(String errorReason);

	/**
	 * fetchInteractive will call this method if the fetch of the
	 * TUMOnlineRequest has succeeded
	 * 
	 * @param rawResponse
	 *            this will be the raw return of the fetch
	 */
	public void onFetch(String rawResponse);

	public void onFetchCancelled();

	/**
	 * if the fetchInteractive method will result in null or there is no
	 * internet connection then this method will be called
	 * 
	 * @param errorReason
	 *            the reason why the request failed (localized)
	 */
	public void onFetchError(String errorReason);

}
