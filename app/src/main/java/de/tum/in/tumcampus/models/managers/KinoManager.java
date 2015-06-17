package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.KinoCard;
import de.tum.in.tumcampus.models.Kino;

/**
 * TU Kino Manager, handles content and card creation
 */
public class KinoManager implements Card.ProvidesCard {

    private static final int TIME_TO_SYNC = 1800; // 1/2 hour
    private final Context mContext;

    private static final String KINO_URL = "https://tumcabe.in.tum.de/Api/kino/";

    /**
     * Database connection
     */
    private SQLiteDatabase db;

    /**
     * Constructor open/create database
     * @param context Context
     */
    public KinoManager(Context context){
        db = DatabaseManager.getDb(context);
        mContext = context;

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS kino (id INTEGER PRIMARY KEY, title TEXT, year VARCHAR, runtime VARCHAR," +
                "genre VARCHAR, director TEXT, actors TEXT, rating VARCHAR, description TEXT, cover TEXT, trailer TEXT, date VARCHAR, created VARCHAR," +
                "link TEXT)");
    }


    /**
     * Removes all old items (older than 3 months)
     */
    void cleanupDb() {
        db.execSQL("DELETE FROM kino WHERE date < date('now','-3 month')");
    }


    /**
     * download kino from external interface (JSON)
     * @param force True to force download over normal sync period, else false
     * @throws Exception
     */
    public void downloadFromExternal(boolean force) throws Exception {

        if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
            return;
        }

        NetUtils net = new NetUtils(mContext);

        // download from kino database
        JSONArray jsonArray = net.downloadJsonArray(KINO_URL, CacheManager.VALIDITY_ONE_DAY, force);

        // remove old items
        cleanupDb();

        if (jsonArray == null){
            return;
        }

        db.beginTransaction();
        try{
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject obj = jsonArray.getJSONObject(i);
                replaceIntoDb(getFromJson(obj));
            }
            SyncManager.replaceIntoDb(db, this);
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

    }

    /**
     * Convert JSON object to Kino
     * @param json JsonObject from external
     * @return Kino
     * @throws Exception
     */
    private static Kino getFromJson(JSONObject json) throws Exception {
        String id = json.getString(Const.JSON_NEWS);
        String title = json.getString(Const.JSON_TITLE);
        String year = json.getString(Const.JSON_YEAR);
        String runtime = json.getString(Const.JSON_RUNTIME);
        String genre = json.getString(Const.JSON_GENRE);
        String director = json.getString(Const.JSON_DIRECTOR);
        String actors = json.getString(Const.JSON_ACTORS);
        String rating = json.getString(Const.JSON_RATING);
        String description = json.getString(Const.JSON_DESCRIPTION);
        String cover = json.getString(Const.JSON_COVER);
        String trailer = json.getString(Const.JSON_TRAILER);
        Date date = Utils.getISODateTime(json.getString(Const.JSON_DATE));
        Date created = Utils.getISODateTime(json.getString(Const.JSON_CREATED));
        String link = json.getString(Const.JSON_LINK);

        return new Kino(id, title, year, runtime, genre, director, actors, rating, description, cover, trailer, date, created, link);
    }


    /**
     * get everything from the database
     * @return Cursor
     */
    public Cursor getAllFromDb(){
        return db.rawQuery("SELECT * from kino",null);
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
                new String[]{k.id, k.title, k.year, k.runtime, k.genre, k.director, k.actors, k.rating,
                        k.description, k.cover, k.trailer, Utils.getDateTimeString(k.date),
                        Utils.getDateTimeString(k.created), k.link});
    }


    /**
     * Add Kino Card to the stream
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context){
        Card card = new KinoCard(context);
        card.apply();
    }
}
