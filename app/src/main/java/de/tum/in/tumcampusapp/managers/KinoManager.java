package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

/**
 * TU Kino Manager, handles content
 */
public class KinoManager extends AbstractManager {

    private static final int TIME_TO_SYNC = 1800; // 1/2 hour
    private static final String KINO_URL = "https://tumcabe.in.tum.de/Api/kino/";

    /**
     * Constructor open/create database
     *
     * @param context Context
     */
    public KinoManager(Context context) {
        super(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS kino (id INTEGER PRIMARY KEY, title TEXT, year VARCHAR, runtime VARCHAR," +
                   "genre VARCHAR, director TEXT, actors TEXT, rating VARCHAR, description TEXT, cover TEXT, trailer TEXT, date VARCHAR, created VARCHAR," +
                   "link TEXT)");

        // remove old items
        cleanupDb();
    }

    /**
     * Removes all old items
     */
    final void cleanupDb() {
        db.execSQL("DELETE FROM kino WHERE date < date('now')");
    }

    /**
     * download kino from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     * @throws JSONException
     */
    public void downloadFromExternal(boolean force) throws JSONException {
        SyncManager sync = new SyncManager(mContext);
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        NetUtils net = new NetUtils(mContext);

        // download from kino database
        Optional<JSONArray> jsonArray = net.downloadJsonArray(KINO_URL + getLastId(), CacheManager.VALIDITY_ONE_DAY, force);

        if (!jsonArray.isPresent()) {
            return;
        }

        JSONArray arr = jsonArray.get();
        // write data to database on device
        db.beginTransaction();
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                replaceIntoDb(getFromJson(obj));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        sync.replaceIntoDb(this);
    }

    /**
     * Convert JSON object to Kino
     *
     * @param json JsonObject from external
     * @return Kino
     * @throws JSONException
     */
    private static Kino getFromJson(JSONObject json) throws JSONException {
        String id = json.getString(Const.JSON_KINO);
        String title = json.getString(Const.JSON_TITLE);
        String year = json.getString(Const.JSON_YEAR);
        String runtime = json.getString(Const.JSON_RUNTIME);
        String genre = json.getString(Const.JSON_GENRE);
        String director = json.getString(Const.JSON_DIRECTOR);
        String actors = json.getString(Const.JSON_ACTORS);
        String rating = json.getString(Const.JSON_RATING);
        String description = json.getString(Const.JSON_DESCRIPTION)
                                 .replaceAll("\n", "")
                                 .trim();
        String cover = json.getString(Const.JSON_COVER);
        String trailer = json.getString(Const.JSON_TRAILER);
        Date date = Utils.getISODateTime(json.getString(Const.JSON_DATE));
        Date created = Utils.getISODateTime(json.getString(Const.JSON_CREATED));
        String link = json.getString(Const.JSON_LINK);

        return new Kino(id, title, year, runtime, genre, director, actors, rating, description, cover, trailer, date, created, link);
    }

    /**
     * get everything from the database
     *
     * @return Cursor
     */
    public Cursor getAllFromDb() {
        return db.rawQuery("SELECT * FROM kino", null);
    }

    /**
     * replace or insert an event in the database
     *
     * @param k Kino obj
     */
    void replaceIntoDb(Kino k) {
        Utils.logv(k.toString());

        db.execSQL("REPLACE INTO kino (id, title, year, runtime, genre, director, actors, rating," +
                   "description, cover, trailer, date, created, link) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                   new String[]{k.getId(), k.getTitle(), k.getYear(), k.getRuntime(), k.getGenre(), k.getDirector(), k.getActors(), k.getRating(),
                                k.getDescription(), k.getCover(), k.getTrailer(), Utils.getDateTimeString(k.getDate()),
                                Utils.getDateTimeString(k.getCreated()), k.getLink()});
    }

    // returns the last id in the database
    private String getLastId() {
        String lastId = "";
        try (Cursor c = db.rawQuery("SELECT id FROM kino ORDER BY id DESC LIMIT 1", null)) {
            if (c.moveToFirst()) {
                lastId = c.getString(0);
            }
        }
        return lastId;
    }

}
