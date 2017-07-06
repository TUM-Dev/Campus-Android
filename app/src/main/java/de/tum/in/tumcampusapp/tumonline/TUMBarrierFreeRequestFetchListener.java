package de.tum.in.tumcampusapp.tumonline;

import java.util.List;

import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;

/**
 * Define how to interact with TUMBarrierFreeRequest by activities.
 */
public interface TUMBarrierFreeRequestFetchListener<T> {

    void onNoInternetError();

    /**
     * fetchInteractive will call this method if the fetch of the
     * TUMRoomFinderRequest has succeeded
     *
     * @param result this will be the raw return of the fetch
     */
    void onFetch(List<T> result);

    /**
     * if the fetchInteractive method will result in null or there is no
     * internet connection then this method will be called
     *
     * @param errorReason the reason why the request failed (localized)
     */
    void onFetchError(String errorReason);
}
