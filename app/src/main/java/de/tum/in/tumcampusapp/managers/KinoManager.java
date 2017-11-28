package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dataAccessObjects.KinoDao;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;

/**
 * TU Kino Manager, handles content
 */
public class KinoManager {

    private static final int TIME_TO_SYNC = 1800; // 1/2 hour
    private static final String KINO_URL = "https://tumcabe.in.tum.de/Api/kino/";

    private final Context mContext;
    private final KinoDao dao;

    /**
     * Constructor open/create database
     *
     * @param context Context
     */
    public KinoManager(Context context) {
        mContext = context.getApplicationContext();
        dao = TcaDb.getInstance(context)
                   .kinoDao();
        // remove old items
        dao.cleanUp();
    }

    /**
     * download kino from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     * @throws JSONException when the downloaded json was invalid
     */
    public void downloadFromExternal(boolean force) throws JSONException {
        SyncManager sync = new SyncManager(mContext);
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        NetUtils net = new NetUtils(mContext);

        // download from kino database
        Optional<JSONArray> jsonArray = net.downloadJsonArray(KINO_URL + dao.getLastId(), CacheManager.VALIDITY_ONE_DAY, force);

        if (!jsonArray.isPresent()) {
            return;
        }

        JSONArray arr = jsonArray.get();
        // write data to database on device
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            dao.insert(getFromJson(obj));
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
}
