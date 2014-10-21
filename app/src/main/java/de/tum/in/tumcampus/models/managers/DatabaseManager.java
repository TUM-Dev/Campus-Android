package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import de.tum.in.tumcampus.auxiliary.Const;

/**
 * Database singleton
 */
public abstract class DatabaseManager {

	/** Database connection */
	private static SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 *
	 * @param c Context
	 * @return SQLiteDatabase Db
	 */
	public static SQLiteDatabase getDb(Context c) {
		if (db == null) {
			File f = c.getDatabasePath(Const.DATABASE_NAME);
            f.getParentFile().mkdirs();
			db = SQLiteDatabase.openDatabase(c.getDatabasePath(Const.DATABASE_NAME).toString(),
                    null, SQLiteDatabase.CREATE_IF_NECESSARY);
		}
		return db;
	}

    public static void resetDb(Context c) {
        getDb(c);
        db.execSQL("DROP TABLE IF EXISTS cache");
        db.execSQL("DROP TABLE IF EXISTS cafeterias");
        db.execSQL("DROP TABLE IF EXISTS cafeterias_menus");
        db.execSQL("DROP TABLE IF EXISTS calendar");
        db.execSQL("DROP TABLE IF EXISTS kalendar_events");
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS news");
        db.execSQL("DROP TABLE IF EXISTS news_sources");
        db.execSQL("DROP TABLE IF EXISTS recents");
        db.execSQL("DROP TABLE IF EXISTS room_locations");
        db.execSQL("DROP TABLE IF EXISTS syncs");
        db.execSQL("DROP TABLE IF EXISTS suggestions_lecture");
        db.execSQL("DROP TABLE IF EXISTS suggestions_mvv");
        db.execSQL("DROP TABLE IF EXISTS suggestions_persons");
        db.execSQL("DROP TABLE IF EXISTS suggestions_rooms");
        db.execSQL("DROP TABLE IF EXISTS unsent_chat_message");
        db.execSQL("DROP TABLE IF EXISTS chat_message");
        db.execSQL("DROP TABLE IF EXISTS chat_room");
    }
}