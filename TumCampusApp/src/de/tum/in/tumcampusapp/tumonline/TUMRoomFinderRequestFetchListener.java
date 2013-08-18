package de.tum.in.tumcampusapp.tumonline;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * this interface sets how to implement a variety of event listeners for the
 * TUMRoomFinderRequest
 * 
 * @author Anas Chakfeh
 */

public interface TUMRoomFinderRequestFetchListener {

	public void onCommonError(String errorReason);

	/**
	 * fetchInteractive will call this method if the fetch of the
	 * TUMRoomFinderRequest has succeeded
	 * 
	 * @param result
	 *            this will be the raw return of the fetch
	 */
	public void onFetch(ArrayList<HashMap<String, String>> result);

	public void onFetchCancelled();

	public void onFetchDefaultMapId(String result);

	/**
	 * if the fetchInteractive method will result in null or there is no
	 * internet connection then this method will be called
	 * 
	 * @param errorReason
	 *            the reason why the request failed (localized)
	 */
	public void onFetchError(String errorReason);

}
