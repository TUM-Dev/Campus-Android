package de.tum.in.tumcampusapp.component.ui.studyroom;

import android.content.Context;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoom;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.CacheManager;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

/**
 * Handles content for the study room feature, fetches external data.
 */
public class StudyRoomGroupManager {

    public static final String STUDYROOM_HOST = "https://www.devapp.it.tum.de";
    private static final String STUDYROOM_URL = STUDYROOM_HOST + "/iris/ris_api.php?format=json";

    private final StudyRoomDao dao;
    private final StudyRoomGroupDao groupDao;
    private final Context mContext;

    StudyRoomGroupManager(Context context) {
        mContext = context;
        dao = TcaDb.getInstance(context)
                   .studyRoomDao();
        groupDao = TcaDb.getInstance(context)
                        .studyRoomGroupDao();
    }

    private static List<StudyRoomGroup> getAllFromJson(JSONObject jsonObject) throws JSONException {
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
            JSONObject jsonObject = allRooms.getJSONObject(i);
            int roomNumber = jsonObject.getInt("raum_nr");
            for (int j = 0; j < groupRoomList.length(); j++) {
                if (groupRoomList.getInt(j) == roomNumber) {
                    StudyRoom studyRoom = new StudyRoom(
                            roomNumber,
                            jsonObject.getString("raum_code"),
                            jsonObject.getString("raum_name"),
                            jsonObject.getString("gebaeude_name"),
                            -1,
                            DateTimeUtils.INSTANCE.getDateTime(jsonObject.getString("belegung_bis"))
                    );
                    studyRooms.add(studyRoom);
                }
            }
        }
        return studyRooms;
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
            for (StudyRoom room : group.getRooms()) {
                room.setStudyRoomGroup(group.getId());
                dao.insert(room);
            }
        }
        new SyncManager(mContext).replaceIntoDb(this);
    }

    public List<StudyRoom> getAllStudyRoomsForGroup(int groupId) {
        return dao.getAll(groupId);
    }

    public List<StudyRoomGroup> getAllFromDb() {
        return groupDao.getAll();
    }
}
