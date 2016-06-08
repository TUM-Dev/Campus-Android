package de.tum.in.tumcampusapp.models.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.Card;
import de.tum.in.tumcampusapp.cards.SurveyCard;
import de.tum.in.tumcampusapp.models.CalendarRowSet;
import de.tum.in.tumcampusapp.models.Faculty;
import de.tum.in.tumcampusapp.models.Kino;
import de.tum.in.tumcampusapp.models.News;

/**
 * Created by aser on 5/5/16.
 */
public class SurveyManager extends AbstractManager implements Card.ProvidesCard{

    private static int TIME_TO_SYNC = 1800; // weiss nicht wie oft
    private static final String FACULTY_URL = "https://tumcabe.in.tum.de/Api/faculty";

    public SurveyManager(Context context){
        super(context);

        db.execSQL("CREATE TABLE IF NOT EXISTS surveyQuestions (id INTEGER PRIMARY KEY, question VARCHAR, yes BOOLEAN, no BOOLEAN, flagged BOOLEAN, answered BOOLEAN, synced BOOLEAN)");
        db.execSQL("CREATE TABLE IF NOT EXISTS faculties (faculty INTEGER, name VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS survey1 (id INTEGER PRIMARY KEY AUTOINCREMENT, date VARCHAR,userID VARCHAR, question TEXT, faculties TEXT, "
                + "yes INTEGER,  no INTEGER, flags INTEGER)");
        //generateTestData(); // Untill the API is done
        //dropTestData(); // untill the API is done
    }

    @Override
    public void onRequestCard(Context context) {
        Cursor rows = getNextQuestions();
        if(rows.moveToFirst()){
            SurveyCard card = new SurveyCard(context);
            card.seQuestions(rows);
            card.apply();
        }
    }

    public Cursor getAllFaculties() {
        return db.rawQuery("SELECT * FROM faculties", null);
    }



    // Get the relevant Questions for the Survey Card (not answered)
    public Cursor getNextQuestions() {
        return db.rawQuery("SELECT id, question, yes, no, flagged, answered, synced FROM surveyQuestions where answered=0", null);
    }

    // For testing purposes untill the API is done
    public void generateTestData(){
        ContentValues cv = new ContentValues(7);
        for (int i = 0; i < 10; i++)
        {
            cv.put("id", i);
            cv.put("question", "Question "+ i);
            cv.put("yes", 0);
            cv.put("no", 0);
            cv.put("flagged", 0);
            cv.put("answered", 0);
            cv.put("synced", 0);
            db.insert("surveyQuestions", null, cv);
        }
    }

    // For Testing Purposes untill the API is done
    public void dropTestData(){
        db.delete("surveyQuestions",null,null);
    }

    /**
     * updates the field of a given question
     * @param question
     * @param updateField: yes || no || flag
     */
    public void updateQuestion(SurveyCard.Question question,String updateField) {
        ContentValues cv = new ContentValues();
        cv.put(updateField, "1");
        cv.put("answered", "1");
        db.update("surveyQuestions",cv,"id = ?",new String[] {question.getQuestionID()+""});
        //db.execSQL("UPDATE surveyQuestions SET ?=1, answered=1 WHERE id=?",
          //      new String[]{updateField,"" + question.getQuestionID()});
    }

    public void insertOwnQuestions(String date, String userID, String question, String faculties){
        ContentValues cv = new ContentValues(8);
        cv.put("date", date);
        cv.put("userID", userID);
        cv.put("question", question);
        cv.put("faculties", faculties);
        cv.put("yes", 0);
        cv.put("no", 0);
        cv.put("flags", 0);
        db.insert("survey1", null, cv);
    }

    // Helpfunction used for testing in Survey Acitvity untill the API is implemented
    public Cursor numberOfQuestionsFrom(String weekago){
        return db.rawQuery("SELECT COUNT(*) FROM survey1 WHERE date >= '"+weekago+"'", null);
    }

    // Helpfunction used for testing in Survey Acitvity untill the API is implemented
    public Cursor lastDateFromLastWeek(String weekAgo){
        return db.rawQuery("SELECT date FROM survey1 WHERE date >= '"+weekAgo+"'", null);
    }

    public Cursor getFacultyID(String facultyName){
        return db.rawQuery("SELECT faculty FROM faculties WHERE name=?", new String[]{facultyName});
    }

    public void downloadFromExternal(boolean force) throws Exception {
        Log.i("Test","DownloadFromExternalSurveyManager");

        if (!force && !SyncManager.needSync(db, this, TIME_TO_SYNC)) {
            return;
        }

        NetUtils net = new NetUtils(mContext);
        // Load all faculties
        JSONArray jsonArray = net.downloadJsonArray(FACULTY_URL, CacheManager.VALIDITY_ONE_DAY, force);
        if(jsonArray==null) {
            return;
        }

        db.beginTransaction();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                replaceIntoDb(getFromJson(obj));
            }
            SyncManager.replaceIntoDb(db, this);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void replaceIntoDb(Faculty f) {
        db.execSQL("REPLACE INTO faculties (faculty, name) VALUES (?, ?)",
                new String[]{f.getId(), f.getName()});
    }

    private static Faculty getFromJson(JSONObject json) throws Exception {
        String id = json.getString(Const.JSON_FACULTY);
        String name = json.getString(Const.JSON_FACULTY_NAME);
        return new Faculty(id, name);
    }

}
