package de.tum.in.tumcampusapp.models.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.tum.in.tumcampusapp.cards.Card;
import de.tum.in.tumcampusapp.cards.SurveyCard;


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




    public Cursor getNextQuestions() {
        // kein Boolean: http://sqlite.org/datatype3.html
        return db.rawQuery("SELECT id, question, yes, no, flagged, answered, synced FROM surveyQuestions where answered=0", null);
    }

    // When seeing questions for card: we shall receive questions and insert them for card

    // Before submitting a question we want to fetch the faculties from the server, store them in the faculty table and map before posting


}
