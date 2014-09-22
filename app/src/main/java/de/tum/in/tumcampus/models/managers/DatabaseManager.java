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
}