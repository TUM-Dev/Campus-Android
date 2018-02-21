package de.tum.in.tumcampusapp.api.tumonline;

/**
 * this interface should frame, how to implement a listener for the
 * fetchInteractive method in TUMOnlineRequest
 */
public interface TUMOnlineRequestFetchListener<T> {

    /**
     * fetchInteractive will call this method if the fetch canceled because of an error
     */
    void onNoInternetError();

    /**
     * fetchInteractive will call this method if the fetch of the
     * TUMOnlineRequest has succeeded
     *
     * @param response de-serialized result object
     */
    void onFetch(T response);

    /**
     * Called if the fetch was canceled by the user. This should be implemented
     * to cancel the HTMLRequest and clear all previous fetching data.
     */
    void onFetchCancelled();

    /**
     * if the fetchInteractive method will result in null or there is no
     * internet connection then this method will be called
     *
     * @param errorReason the reason why the request failed (localized)
     */
    void onFetchError(String errorReason);

    /**
     * Called if there was no error accessing tum online but there are no results to show (no entries)
     */
    void onNoDataToShow();

}
