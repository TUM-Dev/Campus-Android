package de.tum.in.tumcampusapp.models.managers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.tum.in.tumcampusapp.cards.Card;
import de.tum.in.tumcampusapp.cards.SurveyCard;
import de.tum.in.tumcampusapp.models.ChatRoom;


/**
 * Created by aser on 5/5/16.
 */
public class SurveyManager implements Card.ProvidesCard{
    private static int TIME_TO_SYNC = 1800;
    private final Context mContext;

    private SQLiteDatabase db;

    public SurveyManager(Context context){
        db = DatabaseManager.getDb(context);
        this.mContext = context;
        db.execSQL("CREATE TABLE IF NOT EXISTS surveyQuestions (id INTEGER PRIMARY KEY, question VARCHAR, yes BOOLEAN, no BOOLEAN, flagged BOOLEAN, answered BOOLEAN, synced BOOLEAN)");
        db.execSQL("CREATE TABLE IF NOT EXISTS faculties (faculty INTEGER, name VARCHAR)");
        generateTestData(); // Untill the API is done
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
}
