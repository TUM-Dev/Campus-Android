package de.tum.in.tumcampusapp.tumonline;

import com.google.common.base.Optional;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * this interface sets how to implement a variety of event listeners for the
 * TUMRoomFinderRequest
 */

public interface TUMFacilityLocatorRequestFetchListener {

	void onNoInternetError();

	/**
	 * fetchInteractive will call this method if the fetch of the
	 * TUMRoomFinderRequest has succeeded
	 * 
	 * @param result this will be the raw return of the fetch
	 */
	void onFetch(Optional<JSONArray> result);

	/**
	 * if the fetchInteractive method will result in null or there is no
	 * internet connection then this method will be called
	 * 
	 * @param errorReason the reason why the request failed (localized)
	 */
	void onFetchError(String errorReason);

}
