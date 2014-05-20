package de.tum.in.tumcampus.tumonline;

/**
 * this interface should frame, how to implement a listener for the
 * fetchInteractive method in TUMOnlineRequest
 * 
 * @author Daniel G. Mayr, Sascha Moecker
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

	/**
	 * Called if the fetch was canceld by the user. This should be implemented
	 * to cancel the HTMLRequest and clear all previous fetching data.
	 */
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
