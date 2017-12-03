package de.tum.in.tumcampusapp.shadows;

import android.content.Context;
import android.database.Cursor;

import org.json.JSONObject;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.KinoManager;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

/**
 * KinoManager shadow class mainly used because of PowerMockito and Robolectric non-compliance:
 * https://github.com/robolectric/robolectric/issues/3549
 */
@Implements(KinoManager.class)
public class KinoManagerShadow {

    public static Cursor returnedCursor = null;

    @Implementation
    public void __constructor__(Context context) { }

    @Implementation
    final void cleanupDb() { }

    @Implementation
    public void downloadFromExternal(boolean force)  { }

    @Implementation
    private static Kino getFromJson(JSONObject json) {
        return new Kino("id", "title", "year", "runtime", "genre",
                        "director", "actors", "rating", "description",
                        "cover", "trailer", Utils.getISODateTime("today"),
                        Utils.getISODateTime("today"), "link");
    }

    @Implementation
    public Cursor getAllFromDb() {
        return returnedCursor;
    }

    @Implementation
    void replaceIntoDb(Kino k) { }

    @Implementation
    private String getLastId() { return "0"; }
}
