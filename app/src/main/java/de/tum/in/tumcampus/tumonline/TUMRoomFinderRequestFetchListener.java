package de.tum.in.tumcampus.tumonline;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * this interface sets how to implement a variety of event listeners for the
 * TUMRoomFinderRequest
 */

public interface TUMRoomFinderRequestFetchListener {

	void onNoInternetError();

	/**
	 * fetchInteractive will call this method if the fetch of the
	 * TUMRoomFinderRequest has succeeded
	 * 
	 * @param result this will be the raw return of the fetch
	 */
	void onFetch(ArrayList<HashMap<String, String>> result);

	/**
	 * if the fetchInteractive method will result in null or there is no
	 * internet connection then this method will be called
	 * 
	 * @param errorReason the reason why the request failed (localized)
	 */
	void onFetchError(String errorReason);

}
