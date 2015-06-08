package de.tum.in.tumcampus.models.managers;

import android.content.Context;

/**
 * Created by carlodidomenico on 08/06/15.
 * Interface that implements the MockObject Pattern in order to have a mockobject that will represent
 * the calls to the MoodleAPI.
 */
public interface MoodleManager {
    public void requestUserToken(Context currentContext, String username, String password);

}
