package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoom;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoomGroup;

/**
 * Handles content for the study room feature, fetches external data.
 */
public class StudyRoomGroupManager extends AbstractManager {

    public static final String STUDYROOM_HOST = "www.devapp.it.tum.de";
    public static final String STUDYROOM_URL = "https://" + STUDYROOM_HOST + "/iris/ris_api.php?format=json";
    public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public StudyRoomGroupManager(Context context) {
        super(context);
        createStudyRoomGroupTable();
        createStudyRoomTable();
    }

    private final void createStudyRoomGroupTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS study_room_groups " +
                   "(id INTEGER PRIMARY KEY, name VARCHAR, details VARCHAR)");
    }

    private final void createStudyRoomTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS study_rooms " +
                   "(id INTEGER PRIMARY KEY, code VARCHAR, name VARCHAR, location VARCHAR, " +
                   "occupied_till VARCHAR, " +
                   "group_id INTEGER)");
    }

    public void downloadFromExternal() throws JSONException {
        Optional<JSONObject> jsonObject = new NetUtils(mContext).downloadJsonObject(STUDYROOM_URL, CacheManager.VALIDITY_DO_NOT_CACHE, true);
        if (!jsonObject.isPresent()) {
            return;
        }

        removeCache();

        db.beginTransaction();
        try {
            List<StudyRoomGroup> groups = getAllFromJson(jsonObject.get());
            for (StudyRoomGroup group : groups) {
                replaceIntoDb(group);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        new SyncManager(mContext).replaceIntoDb(this);
    }

    /**
     * Replaces/Inserts both study room group and its studyrooms into db.
     *
     * @param studyRoomGroup the study room group that should be saved/updated.
     */
    private void replaceIntoDb(StudyRoomGroup studyRoomGroup) {
        db.execSQL("REPLACE INTO study_room_groups(id, name, details) VALUES" +
                   " (?, ?, ?)",
                   new String[]{String.valueOf(studyRoomGroup.id), studyRoomGroup.name,
                                studyRoomGroup.details});
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATEFORMAT, Locale.US);
        for (StudyRoom studyRoom : studyRoomGroup.rooms) {
            db.execSQL("REPLACE INTO study_rooms(id, code, name, location, occupied_till, " +
                       "group_id) VALUES " +
                       "(?, ?, ?, ?, ?, ?)",
                       new String[]{String.valueOf(studyRoom.id), studyRoom.code, studyRoom.name,
                                    studyRoom.location,
                                    dateFormatter.format(studyRoom.occupiedTill), String.valueOf
                                            (studyRoomGroup.id)});
        }
    }

    public static List<StudyRoomGroup> getAllFromJson(JSONObject jsonObject) throws JSONException {
        JSONArray groupsJsonArray = jsonObject.getJSONArray("gruppen");
        JSONArray roomsJsonArray = jsonObject.getJSONArray("raeume");
        List<StudyRoomGroup> studyRoomGroups = new ArrayList<>();

        for (int i = 0; i < groupsJsonArray.length(); i++) {
            JSONObject groupJsonObject = groupsJsonArray.getJSONObject(i);
            List<StudyRoom> studyRoomList = getStudyRoomsFromJson(groupJsonObject.getJSONArray
                    ("raeume"), roomsJsonArray);
            studyRoomGroups.add(new StudyRoomGroup(
                    groupJsonObject.getInt("nr"),
                    groupJsonObject.getString("name"),
                    groupJsonObject.getString("detail"),
                    studyRoomList
            ));
        }

        return studyRoomGroups;
    }

    private static List<StudyRoom> getStudyRoomsFromJson(JSONArray groupRoomList, JSONArray allRooms)
            throws JSONException {
        List<StudyRoom> studyRooms = new ArrayList<>();

        for (int i = 0; i < allRooms.length(); i++) {
            int roomNumber = allRooms.getJSONObject(i)
                                     .getInt("raum_nr");
            for (int j = 0; j < groupRoomList.length(); j++) {
                if (groupRoomList.getInt(j) == roomNumber) {
                    StudyRoom studyRoom;
                    try {
                        studyRoom = new StudyRoom(
                                roomNumber,
                                allRooms.getJSONObject(i)
                                        .getString("raum_code"),
                                allRooms.getJSONObject(i)
                                        .getString("raum_name"),
                                allRooms.getJSONObject(i)
                                        .getString("gebaeude_name"),
                                new SimpleDateFormat(DATEFORMAT, Locale.US).parse(allRooms
                                                                                          .getJSONObject(i)
                                                                                          .getString("belegung_bis"))
                        );
                    } catch (ParseException e) {
                        //Room is not occupied

                        studyRoom = new StudyRoom(
                                roomNumber,
                                allRooms.getJSONObject(i)
                                        .getString("raum_code"),
                                allRooms.getJSONObject(i)
                                        .getString("raum_name"),
                                allRooms.getJSONObject(i)
                                        .getString("gebaeude_name"),
                                new Date()
                        );
                    }
                    studyRooms.add(studyRoom);
                }
            }
        }
        return studyRooms;
    }

    private void removeCache() {
        db.execSQL("DELETE FROM study_room_groups");
        db.execSQL("DELETE FROM study_rooms");
    }

    /**
     * Returns all study room groups
     *
     * @return Database cursor (id, name, details)
     */
    public Cursor getAllFromDb() {
        return db.query("study_room_groups", null, null, null, null, null, null);
    }

    /**
     * Retrieves all study room groups from a cursor, ordered by name.
     */
    public List<StudyRoomGroup> getStudyRoomGroupsFromCursor(Cursor cursor) {
        List<StudyRoomGroup> studyRoomGroups = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                studyRoomGroups.add(new StudyRoomGroup(cursor.getInt(0), cursor.getString(1),
                                                       cursor.getString(2), getStudyRoomsFromCursor(getStudyRoomsFromDb(cursor
                                                                                                                                .getInt(0)))));
            } while (cursor.moveToNext());
        }
        Collections.sort(studyRoomGroups);

        return studyRoomGroups;
    }

    /**
     * Retrieves all study rooms of a given group from db, ordered by occupation status.
     */
    public Cursor getStudyRoomsFromDb(int studyRoomGroupId) {
        return db.rawQuery("SELECT id as _id, code, name, location, occupied_till, group_id FROM " +
                           "study_rooms WHERE group_id = ? ORDER BY occupied_till ASC", new String[]{String
                                                                                                             .valueOf(studyRoomGroupId)});
    }

    private static List<StudyRoom> getStudyRoomsFromCursor(Cursor cursor) {
        List<StudyRoom> studyRooms = new ArrayList<>(cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                studyRooms.add(getStudyRoomFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return studyRooms;
    }

    /**
     * Gets a single study room from a cursor, if possible. Note: This won't move the cursor
     * forward.
     *
     * @param cursor the cursor which contains study room information
     * @return study room
     */
    public static StudyRoom getStudyRoomFromCursor(Cursor cursor) {
        StudyRoom studyRoom = null;
        try {
            studyRoom = new StudyRoom(cursor.getInt(0), cursor.getString(1), cursor.getString
                    (2), cursor.getString(3), new SimpleDateFormat(DATEFORMAT, Locale.US).parse(cursor
                                                                                                        .getString(4)));
        } catch (ParseException e) {
            Utils.log(e);
        }
        return studyRoom;
    }
}
