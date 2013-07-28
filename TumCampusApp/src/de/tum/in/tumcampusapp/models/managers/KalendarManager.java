package de.tum.in.tumcampusapp.models.managers;

import java.util.ArrayList;
import java.util.Iterator;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.KalendarRow;
import de.tum.in.tumcampusapp.models.KalendarRowSet;
import de.tum.in.tumcampusapp.models.LectureItem;

public class KalendarManager {
	private SQLiteDatabase db;
	public KalendarManager(Context context) {
		db = DatabaseManager.getDb(context);

	// create table if needed
		db.execSQL("CREATE TABLE IF NOT EXISTS kalendar_events ("
				+ "nr VARCHAR PRIMARY KEY, status VARCHAR, url VARCHAR, "
				+ "title VARCHAR, description VARCHAR, dtstart VARCHAR, dtend VARCHAR, "
				+ "location VARCHAR, longitude VARCHAR, latitude VARCHAR)");
		new SyncManager(context);
	}
	
	public void importKalendar(String rawResponse){
		//reader for xml
		Serializer serializer = new Persister();

		// KalendarRowSet will contain list of events in KalendarRow
		KalendarRowSet myKalendarList = new KalendarRowSet();
		
		myKalendarList.setKalendarList(new ArrayList<KalendarRow>());


		try {
			//reading xml
			myKalendarList = serializer.read(KalendarRowSet.class,
					rawResponse);
			Iterator itr=myKalendarList.getKalendarList().iterator();
			while(itr.hasNext()){
				KalendarRow row=(KalendarRow)itr.next();
				//insert into database
				try{
					replaceIntoDb(row);
				}catch (Exception e) {
					boolean success = false;
					Log.d("SIMPLEXML", "Error in field: " + e.getMessage());
					e.printStackTrace();
				}
				Log.d("Title Kalendar Row", row.getTitle());
			}

		} catch (Exception e) {
			boolean success = false;
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void replaceIntoDb(KalendarRow row) throws Exception {
		Utils.log(row.toString());

		if (row.getNr().length() == 0) 
			throw new Exception("Invalid id.");
		
		if (row.getTitle().length() == 0) 
			throw new Exception("Invalid lecture Title.");

		if (row.getGeo() != null) 
			db.execSQL(
					"REPLACE INTO kalendar_events (nr, status, url, title, "
							+ "description, dtstart, dtend, location, longitude, latitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
							new String[] { row.getNr(), row.getStatus(),
									row.getUrl(),row.getTitle(),row.getDescription(),row.getDtstart(),row.getDtend(),row.getLocation()
									,row.getGeo().getLongitude(),row.getGeo().getLatitude()});
		else
			db.execSQL(
					"REPLACE INTO kalendar_events (nr, status, url, title, "
							+ "description, dtstart, dtend, location) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
							new String[] { row.getNr(), row.getStatus(),
									row.getUrl(),row.getTitle(),row.getDescription(),row.getDtstart(),row.getDtend(),row.getLocation()
							});
	}
	
	public Cursor getAllFromDb() {
		return db
				.rawQuery(
						"SELECT * FROM kalendar_events",
						null);
	}

}
