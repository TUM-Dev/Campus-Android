package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.tum.in.tumcampus.auxiliary.AccessTokenManager;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.models.LectureItem;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

/**
 * Lecture item Manager, handles database stuff, internal imports
 */
public class LectureItemManager {

	/**
	 * Last insert counter
	 */
	public static int lastInserted = 0;

	public static int TIME_TO_SYNC = 604800; // 1 week

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Additional information for exception messages
	 */
	public String lastInfo = "";

	/**
	 * Constructor, open/create database, create table if necessary
	 * 
	 * <pre>
	 * @param context Context
	 * </pre>
	 */
	public LectureItemManager(Context context) {
		db = DatabaseManager.getDb(context);

		// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS lectures_items ("
				+ "id VARCHAR PRIMARY KEY, lectureId VARCHAR, start VARCHAR, "
				+ "end VARCHAR, name VARCHAR, module VARCHAR, location VARCHAR, "
				+ "note VARCHAR, url VARCHAR, seriesId VARCHAR)");

		new SyncManager(context);
	}

	/**
	 * Delete lecture item from database
	 * 
	 * <pre>
	 * @param id Lecture item ID
	 * </pre>
	 */
	public void deleteItemFromDb(String id) {
		db.execSQL("DELETE FROM lectures_items WHERE id = ?",
				new String[] { id });
	}

	/**
	 * Delete lecture items from database
	 * 
	 * <pre>
	 * @param id Lecture ID
	 * </pre>
	 */
	public void deleteLectureFromDb(String id) {
		db.execSQL("DELETE FROM lectures_items WHERE lectureId = ?",
				new String[] { id });
	}

	/**
	 * Checks if the lectures_items table is empty
	 * 
	 * @return true if no lecture items are available, else false
	 */
	public boolean empty() {
		boolean result = true;
		Cursor c = db.rawQuery("SELECT id FROM lectures_items LIMIT 1", null);
		if (c.moveToNext()) {
			result = false;
		}
		c.close();
		return result;
	}

	/**
	 * Get all lecture items from the database
	 * 
	 * @return Database cursor (name, location, _id)
	 */
	public Cursor getAllFromDb() {
		return db
				.rawQuery(
						"SELECT name, note, location, strftime('%w', start) as weekday, "
								+ "strftime('%H:%M', start) as start_de, strftime('%H:%M', end) as end_de, "
								+ "strftime('%d.%m.%Y', start) as start_dt, strftime('%d.%m.%Y', end) as end_dt, "
								+ "url, lectureId, id as _id FROM lectures_items ORDER BY start",
						null);
	}

	/**
	 * Get all lecture items for a special lecture from the database
	 * 
	 * <pre>
	 * @param lectureId Lecture ID
	 * @return Database cursor (name, note, location, weekday, start_de,
	 * 		   end_de, start_dt, end_dt, url, location, _id)
	 * </pre>
	 */
	public Cursor getAllFromDbForId(String lectureId) {
		return db
				.rawQuery(
						"SELECT name, note, location, strftime('%w', start) as weekday, "
								+ "strftime('%d.%m.%Y %H:%M', start) as start_de, strftime('%H:%M', end) as end_de, "
								+ "strftime('%d.%m.%Y', start) as start_dt, strftime('%d.%m.%Y', end) as end_dt, "
								+ "url, lectureId, id as _id FROM lectures_items WHERE lectureId = ? ORDER BY start",
						new String[] { lectureId });
	}

	/**
	 * Get all lecture items from the database
	 * 
	 * @return Database cursor (name, location, _id)
	 */
	public Cursor getCurrentFromDb() {
		return db
				.rawQuery(
						"SELECT name, location, id as _id "
								+ "FROM lectures_items WHERE datetime('now', 'localtime') BETWEEN start AND end AND "
								+ "lectureId NOT IN ('holiday', 'vacation') LIMIT 1",
						null);
	}

	/**
	 * Get all future lecture items
	 * 
	 * @return Database cursor (name, note, location, start, end, url)
	 */
	public Cursor getFutureFromDb() {
		return db
				.rawQuery(
						"SELECT name, note, location, start, end, url FROM lectures_items "
								+ "WHERE end > datetime('now', 'localtime') ORDER BY start",
						null);
	}

	/**
	 * Get all upcoming and unfinished lecture items from the database
	 * 
	 * @return Database cursor (name, note, location, weekday, start_de, end_de,
	 *         start_dt, end_dt, url, lectureId, _id)
	 */
	public Cursor getRecentFromDb() {
		return db
				.rawQuery(
						"SELECT name, note, location, strftime('%w', start) as weekday, "
								+ "strftime('%H:%M', start) as start_de, strftime('%H:%M', end) as end_de, "
								+ "strftime('%d.%m.%Y', start) as start_dt, strftime('%d.%m.%Y', end) as end_dt, "
								+ "url, lectureId, id as _id FROM lectures_items WHERE end > datetime('now', 'localtime') AND "
								+ "start < date('now', '+7 day') ORDER BY start",
						null);
	}

	/**
	 * Checks if lectures are available
	 * 
	 * @return true if lectures are available, else false
	 */
	public boolean hasLectures() {
		boolean result = false;
		Cursor c = db.rawQuery("SELECT id FROM lectures_items WHERE "
				+ "lectureId NOT IN ('holiday', 'vacation') LIMIT 1", null);
		if (c.moveToNext()) {
			result = true;
		}
		c.close();
		return result;
	}

	/**
	 * this function allows us to import all lecture settings from TUMOnline
	 * 
	 * @todo check not to set any data to null
	 * @throws Exception
	 */
	public void importFromTUMOnline(Context context, boolean force) throws Exception {
		boolean success = true;

		int count = Utils.dbGetTableCount(db, "lectures_items");

		if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
			return;
		}

		db.beginTransaction();
		try {

			// acquire access token
			if (new AccessTokenManager(context).hasValidAccessToken()) {
				throw new Exception("No access token set");
			}

			// get my lectures
			TUMOnlineRequest requestHandler = new TUMOnlineRequest("veranstaltungenEigene", context);
			String strMine = requestHandler.fetch();
			// deserialize
			Serializer serializer = new Persister();

			// define it this way to get at least an empty list
			LecturesSearchRowSet myLecturesList = new LecturesSearchRowSet();
			myLecturesList.setLehrveranstaltungen(new ArrayList<LecturesSearchRow>());

			try {
				myLecturesList = serializer.read(LecturesSearchRowSet.class, strMine);
			} catch (Exception e) {
				success = false;
				Log.d("SIMPLEXML", "wont work: " + e.getMessage());
				e.printStackTrace();
			}

			// get schedule for my lectures
			for (int i = 0; i < myLecturesList.getLehrveranstaltungen().size(); i++) {
				LecturesSearchRow currentLecture = myLecturesList.getLehrveranstaltungen().get(i);

				// now, get termine for each lecture
				TUMOnlineRequest req = new TUMOnlineRequest("veranstaltungenTermine", context);
				req.setParameter("pLVNr", currentLecture.getStp_sp_nr());
				String strTermine = req.fetch();

				// define it this way to assert that there is an empty list
				LectureAppointmentsRowSet MyLectureTerminList = new LectureAppointmentsRowSet();
				MyLectureTerminList.setLehrveranstaltungenTermine(new ArrayList<LectureAppointmentsRow>());
				try {
					MyLectureTerminList = serializer.read(LectureAppointmentsRowSet.class, strTermine);
				} catch (Exception e) {
					success = false;
					Log.d("SIMPLEXML", "wont work: " + e.getMessage());
					e.printStackTrace();
				}

				// now, enter each task date to the current Lecture
				if (MyLectureTerminList != null
						&& MyLectureTerminList.getLehrveranstaltungenTermine() != null)
					for (int y = 0; y < MyLectureTerminList.getLehrveranstaltungenTermine().size(); y++) {
						try {
							// set currentTask
							LectureAppointmentsRow currentTask = MyLectureTerminList
									.getLehrveranstaltungenTermine().get(y);

							// parse dates
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							Date start = formatter.parse(currentTask.getBeginn_datum_zeitpunkt());
							Date end = formatter.parse(currentTask.getEnde_datum_zeitpunkt());

							// check if the task is in the future
							Calendar cnow = Calendar.getInstance();
							Calendar cend = Calendar.getInstance();
							cend.setTime(end);

							// TODO Replaced that, because we fetch all
							// appointments and decide on while fetching if we
							// need those appointments
							if (cnow.before(cend)) {
							}

							// set name/module/ort/lectureId
							String location = "";
							if (currentTask.getOrt() != null)
								location = currentTask.getOrt();
							String name = "";
							if (currentLecture.getTitel() != null)
								name = currentLecture.getTitel();

							String lectureId = "";
							if (currentLecture.getStp_lv_nr() != null)
								lectureId = currentLecture.getStp_lv_nr();

							String module = "";
							if (name.contains("(") && name.contains(")")) {
								module = name.substring(name.indexOf("(") + 1,
										name.indexOf(")"));
								name = name.substring(0, name.indexOf("("))
										.trim();
							}

							// set id
							String id = lectureId + "_"
									+ String.valueOf(start.getTime());

							// set wochentag
							String wochentag = Utils.getWeekDayByDate(start)
									.toUpperCase();

							SimpleDateFormat sdfvon = new SimpleDateFormat(
									"HH:mm");
							String von = sdfvon.format(start);

							String seriesId = lectureId + "_" + wochentag + "_"
									+ von;

							// set note and url
							String note = "";
							if (currentTask.getTermin_betreff() != null)
								note = currentTask.getTermin_betreff();
							String url = "";
							if (lectureId.length() == 0) {
								lectureId = Utils.md5(name);
							}

							// now, make entry to database
							replaceIntoDb(new LectureItem(id, lectureId, start,
									end, name, module, location, note, url,
									seriesId));

						} catch (Exception ex) {
							success = false;
							Log.d("TUMOnlineParseATask", "the task could not be parsed");
							ex.printStackTrace();
						}
					}
			}
			if (success) {
				SyncManager.replaceIntoDb(db, this);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// update last insert counter
		lastInserted += Utils.dbGetTableCount(db, "lectures_items") - count;
	}

	/**
	 * Replace or Insert a lecture item in the database
	 * 
	 * <pre>
	 * @param l LectureItem object
	 * @throws Exception
	 * </pre>
	 */
	public void replaceIntoDb(LectureItem l) throws Exception {
		Utils.log(l.toString());

		if (l.id.length() == 0) {
			throw new Exception("Invalid id.");
		}
		if (l.lectureId.length() == 0) {
			throw new Exception("Invalid lectureId.");
		}
		if (l.name.length() == 0) {
			throw new Exception("Invalid name.");
		}
		if (l.seriesId.length() == 0) {
			throw new Exception("Invalid id.");
		}

		db.execSQL(
				"REPLACE INTO lectures_items (id, lectureId, start, end, "
						+ "name, module, location, note, url, seriesId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new String[] { l.id, l.lectureId,
						Utils.getDateTimeString(l.start),
						Utils.getDateTimeString(l.end), l.name, l.module,
						l.location, l.note, l.url, l.seriesId });
	}
}