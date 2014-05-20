package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Lecture Manager, handles database stuff
 */
public class LectureManager {

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * </pre>
	 */
	public LectureManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS lectures (id VARCHAR PRIMARY KEY, name VARCHAR, module VARCHAR)");
	}

	/**
	 * Delete a lecture from the database
	 * 
	 * <pre>
	 * @param id Lecture ID
	 * </pre>
	 */
	public void deleteItemFromDb(String id) {
		db.execSQL("DELETE FROM lectures WHERE id = ?", new String[] { id });
	}

	/**
	 * Get all lectures from the database
	 * 
	 * @return Database cursor (name, module, _id)
	 */
	public Cursor getAllFromDb() {
		return db.rawQuery(
				"SELECT name, module, id as _id FROM lectures ORDER BY name",
				null);
	}

	/**
	 * Refresh lectures from the lectures_items table
	 */
	public void updateLectures() {
		db.execSQL("REPLACE INTO lectures (id, name, module) "
				+ "SELECT DISTINCT lectureId, name, module FROM lectures_items");
	}
}