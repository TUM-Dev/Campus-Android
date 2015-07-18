package de.tum.in.tumcampus.models.managers;

import de.tum.in.tumcampus.models.MVVObject;

/**
 * Created by enricogiga on 16/06/2015.
 * Delegate to set the callback methods after parsing MVGLive contents
 */
public interface MVVDelegate {
    void showSuggestionList(MVVObject object);

    void showDepartureList(MVVObject object);

    void showError(MVVObject object);
}
