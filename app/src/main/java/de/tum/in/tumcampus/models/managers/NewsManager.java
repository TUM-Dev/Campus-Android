package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.NewsCard;
import de.tum.in.tumcampus.models.News;

/**
 * News Manager, handles database stuff, external imports
 */
public class NewsManager implements Card.ProvidesCard {
    /** Last insert counter */
    public static int lastInserted = 0;

    private static final int TIME_TO_SYNC = 86400; // 1 day

    /**
     * Convert JSON object to News and download news image
     * <p/>
     * Example JSON: e.g. { "id": "162327853831856_174943842570257", "from": {
     * ... }, "message": "Testing ...", "picture":
     * "http://photos-d.ak.fbcdn.net/hphotos-ak-ash4/268937_174943835903591_162327853831856_476156_7175901_s.jpg"
     * , "link":
     * "https://www.facebook.com/photo.php?fbid=174943835903591&set=a.174943832570258.47966.162327853831856&type=1"
     * , "name": "Wall Photos", "icon":
     * "http://static.ak.fbcdn.net/rsrc.php/v1/yz/r/StEh3RhPvjk.gif", "type":
     * "photo", "object_id": "174943835903591", "created_time":
     * "2011-07-04T01:58:25+0000", "updated_time": "2011-07-04T01:58:25+0000" },
     *
     * @param json see above
     * @return News
     * @throws Exception
     */
    private static News getFromJson(JSONObject json) throws Exception {

        String picture = "";
        if (json.has("picture")) {
            picture = json.getString(Const.JSON_PICTURE);
            //target = Utils.getCacheDir("news/cache") + Utils.md5(picture) + ".jpg";
            //Utils.downloadFileThread(picture, target);
        }
        String link = "";
        if (json.has(Const.JSON_LINK) && !json.getString(Const.JSON_LINK).contains(Const.FACEBOOK_URL)) {
            link = json.getString(Const.JSON_LINK);
        }
        if (link.length() == 0 && json.has(Const.JSON_OBJECT_ID)) {
            link = "http://graph.facebook.com/" + json.getString(Const.JSON_OBJECT_ID) + "/Picture?type=normal";
        }

        // message empty => description empty => caption
        String message = "";
        if (json.has(Const.JSON_MESSAGE)) {
            message = json.getString(Const.JSON_MESSAGE);
        } else if (json.has(Const.JSON_DESCRIPTION)) {
            message = json.getString(Const.JSON_DESCRIPTION);
        } else if (json.has(Const.JSON_CAPTION)) {
            message = json.getString(Const.JSON_CAPTION);
        } else if (json.has("name")) {
            message = json.getString("name");
        } else if (json.has("story")) {
            message = json.getString("story");
        } else {
            Utils.log("No message / json object error - FB changed something?");
        }

        Date date = Utils.getDate(json.getString(Const.JSON_CREATED_TIME));

        return new News(json.getString(Const.JSON_ID), message, link, picture, date);
    }

    /** Database connection */
    private final SQLiteDatabase db;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public NewsManager(Context context) {
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS news (id VARCHAR PRIMARY KEY, message VARCHAR, link VARCHAR, "
                + "image VARCHAR, date VARCHAR)");
    }

    /**
     * Removes all old items (older than 3 months)
     */
    void cleanupDb() {
        db.execSQL("DELETE FROM news WHERE date < date('now','-3 month')");
    }

    /**
     * Download news from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     * @throws Exception
     */
    public void downloadFromExternal(boolean force) throws Exception {

        if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
            return;
        }

        String url = "https://graph.facebook.com/162327853831856/feed/?limit=100&access_token=";
        String token = "141869875879732|FbjTXY-wtr06A18W9wfhU8GCkwU";

        JSONArray jsonArray = Utils.downloadJson(url + URLEncoder.encode(token, "UTF-8"))
                .getJSONArray(Const.JSON_DATA);

        cleanupDb();
        int count = Utils.dbGetTableCount(db, "news");

        db.beginTransaction();
        try {
            int countItems = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String[] types = new String[]{"photo", "link", "video"};

                String[] ids = new String[]{
                        "162327853831856_228060957258545",
                        "162327853831856_228060957258545",
                        "162327853831856_224344127630228"};

                // filter out events, empty items
                if ((!Arrays.asList(types).contains(obj.getString("type")) && !Arrays
                        .asList(ids).contains(obj.getString("id")))
                        || !obj.getJSONObject(Const.JSON_FROM)
                        .getString(Const.JSON_ID)
                        .equals("162327853831856")) {
                    continue;
                }
                // NTK added Kurz notiert Archiv ---> ignore in news
                if (obj.has("name") && (obj.getString("name").equals("Kurz notiert") || obj
                        .getString("name").equals("Kurz notiert Archiv"))) {
                    continue;
                }

                // NTK added ignore events
                if (obj.has("story") && (obj.getString("story").equals("TUM Campus App created an event."))) {
                    continue;
                }

                if (countItems > 24) {
                    break;
                }
                replaceIntoDb(getFromJson(obj));
                countItems++;
            }
            SyncManager.replaceIntoDb(db, this);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        // update last insert counter
        lastInserted += Utils.dbGetTableCount(db, "news") - count;
    }

    /**
     * Get all news from the database
     *
     * @return Database cursor (image, message, date_de, link, _id)
     */
    public Cursor getAllFromDb() {
        return db.rawQuery("SELECT image, message, strftime('%d.%m.%Y', date) as date_de, link, id as _id "
                + "FROM news ORDER BY date DESC", null);
    }

    /**
     * Removes all cache items
     */
    public void removeCache() {
        db.execSQL("DELETE FROM news");
    }

    /**
     * Replace or Insert a event in the database
     *
     * @param n News object
     * @throws Exception
     */
    void replaceIntoDb(News n) throws Exception {
        Utils.logv(n.toString());

        if (n.id.length() == 0) {
            throw new Exception("Invalid id.");
        }
        if (n.message.length() == 0) {
            Utils.log(n.toString());
            throw new Exception("Invalid message.");
        }
        db.execSQL("REPLACE INTO news (id, message, link, image, date) VALUES (?, ?, ?, ?, ?)",
                new String[]{n.id, n.message, n.link, n.image, Utils.getDateString(n.date)});
    }

    /**
     * Adds the newest news card
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        Cursor cur = getAllFromDb();
        if (cur.moveToFirst()) {
            NewsCard card = new NewsCard(context);
            String title = cur.getString(1);
            if (title.contains("\n")) {
                title = title.substring(0, title.indexOf('\n'));
            }
            card.setNews(cur.getString(0), title, cur.getString(3), cur.getString(2));
            card.apply();
        }
        cur.close();
    }
}