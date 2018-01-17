package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.StudyRoomDao;
import de.tum.in.tumcampusapp.database.dao.StudyRoomGroupDao;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoom;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoomGroup;

/**
 * Handles content for the study room feature, fetches external data.
 */
public class StudyRoomGroupManager extends AbstractManager {

    public static final String STUDYROOM_HOST = "www.devapp.it.tum.de";
    public static final String STUDYROOM_URL = "https://" + STUDYROOM_HOST + "/iris/ris_api.php?format=json";
    public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    private final StudyRoomDao dao;
    private final StudyRoomGroupDao groupDao;

    public StudyRoomGroupManager(Context context) {
        super(context);
        dao = TcaDb.getInstance(context).studyRoomDao();
        groupDao = TcaDb.getInstance(context).studyRoomGroupDao();
    }


    public void downloadFromExternal() throws JSONException {
        Optional<JSONObject> jsonObject = new NetUtils(mContext).downloadJsonObject(STUDYROOM_URL, CacheManager.VALIDITY_DO_NOT_CACHE, true);
        if (!jsonObject.isPresent()) {
            return;
        }
        dao.removeCache();
        groupDao.removeCache();

        List<StudyRoomGroup> groups = getAllFromJson(jsonObject.get());
        for (StudyRoomGroup group : groups) {
            groupDao.insert(group);
            for(StudyRoom room : group.getRooms()){
                room.setStudyRoomGroup(group.getId());
                dao.insert(room);
            }
        }
        new SyncManager(mContext).replaceIntoDb(this);
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
                                -1,
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
                                -1,
                                new Date()
                        );
                    }
                    studyRooms.add(studyRoom);
                }
            }
        }
        return studyRooms;
    }


    public List<StudyRoom> getAllStudyRoomsForGroup(int groupId){
        return dao.getAll(groupId);
    }

    public List<StudyRoomGroup>getAllFromDb() {
        return groupDao.getAll();
    }

}
