package de.tum.in.tumcampusapp.models.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.Card;
import de.tum.in.tumcampusapp.cards.SurveyCard;
import de.tum.in.tumcampusapp.models.Faculty;
import de.tum.in.tumcampusapp.models.Question;
import de.tum.in.tumcampusapp.models.TUMCabeClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SurveyManager extends AbstractManager implements Card.ProvidesCard {


    public SurveyManager(Context context) {
        super(context);
        db.execSQL("CREATE TABLE IF NOT EXISTS faculties (faculty INTEGER, name VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS openQuestions (question INTEGER PRIMARY KEY, text VARCHAR, created VARCHAR, end VARCHAR, answerid INTEGER, answered BOOLEAN, synced BOOLEAN)");
        db.execSQL("CREATE TABLE IF NOT EXISTS ownQuestions (question INTEGER PRIMARY KEY, text VARCHAR, created VARCHAR, end VARCHAR, yes INTEGER, no INTEGER, deleted BOOLEAN, synced BOOLEAN)");
    }

    @Override
    public void onRequestCard(Context context) {
        if (NetUtils.isConnected(mContext)) {
            downLoadOpenQuestions();
        }
        Cursor rows = getUnansweredQuestionsSince(Utils.getDateTimeString(new Date()));
        if (rows.moveToFirst()) {
            SurveyCard card = new SurveyCard(context);
            card.seQuestions(rows);
            card.apply();
        }
    }

    public Cursor getAllFaculties() {
        return db.rawQuery("SELECT * FROM faculties", null);
    }


    // Get relevant questions for Card: unanswered and their end date is still in the future
    public Cursor getUnansweredQuestionsSince(String date) {
        Cursor c = db.rawQuery("SELECT question, text FROM openQuestions WHERE answered=0 AND end >= '"+ date+ "'", null);
        return c;
    }

    // For displaying responses in surveyActivity
    public Cursor getMyOwnQuestionsSince(String date) {
        Cursor c = db.rawQuery("SELECT * FROM ownQuestions where deleted = 0 AND end >= '"+ date+"'", null);
        return c;
    }

    // For deleting responses in response tab in surveyActivity
    public void deleteMyOwnQuestion(int id) {
        TUMCabeClient.getInstance(mContext).deleteOwnQuestion(id, new Callback<Question>() {
            @Override
            public void success(Question q, Response response) {
                Utils.log("TUMCabeClient_delete_question_successeed");
            }

            @Override
            public void failure(RetrofitError error) {
                Utils.log("TUMCabeClient_delete_question_failed. Error: " + error.toString());
            }
        });
        db.execSQL("UPDATE ownQuestions SET deleted=1 WHERE question=" + id);
    }


    /**
     * updates the field of a given question in Survey Card
     *
     * @param question
     * @param answerTag: yes || no || flag || skip
     */
    public void updateQuestion(Question question, int answerTag) {
        ContentValues cv = new ContentValues();

        if (answerTag != 3) {
            cv.put("answerid", answerTag);
        } else {
            cv.put("synced", 1);//Do not sync skipped questions later
        }

        // Set as answered despite of the answerTag
        cv.put("answered", 1);

        //Commit update to database
        try {
            db.beginTransaction();
            db.update("openQuestions", cv, "question = ?", new String[]{question.getQuestion().toString()});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        //Trigger sync if we are connected currently
        if (NetUtils.isConnected(mContext)) {
            syncOpenQuestionsTable();
        }

    }

    // Syncs answered but not yet synced responses with server
    public void syncOpenQuestionsTable() {
        Cursor cursor = db.rawQuery("SELECT question, answerid FROM openQuestions WHERE synced=0 AND answered=1", null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Question answeredQuestion = new Question(cursor.getString(cursor.getColumnIndex("question")), cursor.getInt(cursor.getColumnIndex("answerid")));
                    // Submit Answer to Server
                    if (answeredQuestion != null) {
                        TUMCabeClient.getInstance(mContext).submitAnswer(answeredQuestion, new Callback<Question>() {
                            @Override
                            public void success(Question question, Response response) {
                                Utils.log("Test_resp_submitQues Succeeded: " + response.getBody().toString());
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Utils.log("Test_resp_submitQues Failure" + error.toString());
                            }
                        });
                    }

                    ContentValues cv = new ContentValues();
                    cv.put("synced", "1");
                    db.update("openQuestions", cv, "question = ?", new String[]{cursor.getString(cursor.getColumnIndex("question")) + ""});
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Utils.log(e.toString());
        } finally {
            cursor.close();
        }
    }

    // Helpfunction used for testing in Survey Acitvity untill the API is implemented
    public Cursor numberOfQuestionsFrom(String weekago) {
        return db.rawQuery("SELECT COUNT(*) FROM ownQuestions WHERE created >= '" + weekago + "'", null);
    }

    // Helpfunction used for testing in Survey Acitvity untill the API is implemented
    public Cursor lastDateFromLastWeek(String weekAgo) {
        return db.rawQuery("SELECT created FROM ownQuestions WHERE created >= '" + weekAgo + "'", null);
    }

    public Cursor getFacultyID(String facultyName) {
        return db.rawQuery("SELECT faculty FROM faculties WHERE name=?", new String[]{facultyName});
    }

    public void downloadFacultiesFromExternal() throws Exception {
        ArrayList<Faculty> faculties = TUMCabeClient.getInstance(mContext).getFaculties();

        db.beginTransaction();
        try {
            for (int i = 0; i < faculties.size(); i++) {
                replaceIntoDb(faculties.get(i));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    // For the SurveyCard
    public void downLoadOpenQuestions() {
        ArrayList<Question> openQuestions = new ArrayList<Question>();
        try {
            openQuestions = TUMCabeClient.getInstance(mContext).getOpenQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }

        delteFlaggedQuestions(openQuestions);

        for (int i = 0; i < openQuestions.size(); i++) {
            List<String> openQuestionFaculties = Arrays.asList(openQuestions.get(i).getFacultiesOfOpenQuestions());
            String userMajor = Utils.getInternalSettingString(mContext, "user_major", "");
            // Incase  the user selected the major upon app start, then save the major related questions. Otherwise save all questions
            if (userMajor.equals("0") || openQuestionFaculties.contains(userMajor)) {
                replaceIntoDBOpenQuestions(openQuestions.get(i));
            }
        }
    }

    // Questions from local Database that no longer exists in the fetched Questions (flagged questions) get deleted
    void delteFlaggedQuestions(ArrayList<Question> fetchedOpenedQuestions) {
        ArrayList<Question> downloadedQuestionsID = new ArrayList<Question>();
        for (int x = 0; x < fetchedOpenedQuestions.size(); x++) {
            downloadedQuestionsID.add(new Question(fetchedOpenedQuestions.get(x).getQuestion()));
        }

        Cursor c = db.rawQuery("SELECT question FROM openQuestions", null);
        if (c != null && c.moveToFirst()) {
            do {
                if (!downloadedQuestionsID.contains(new Question(c.getString(c.getColumnIndex("question"))))) {
                    // delete Question from database
                    db.delete("openQuestions", "question = ?", new String[]{c.getString(c.getColumnIndex("question"))});
                }
            } while (c.moveToNext());
        }
    }

    // Inserts new openQuestion if it doesn't exist
    void replaceIntoDBOpenQuestions(Question q) {
        Cursor c = db.rawQuery("SELECT answerid FROM openQuestions WHERE question = ?", new String[]{q.getQuestion()});

        // if question doesn't exist
        if (!c.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put("question", q.getQuestion());
            cv.put("text", q.getText());
            cv.put("created", q.getCreated());
            cv.put("end", q.getEnd());
            cv.put("answerid", 0);
            cv.put("answered", 0);
            cv.put("synced", 0);
            try {
                db.beginTransaction();
                db.insert("openQuestions", null, cv);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                c.close();
            }
        }
    }

    public void downLoadOwnQuestions() {
        ArrayList<Question> ownQuestions = new ArrayList<Question>();
        try {
            ownQuestions = TUMCabeClient.getInstance(mContext).getOwnQuestions();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log(e.toString());
        }

        for (int i = 0; i < ownQuestions.size(); i++) {
            replaceIntoDbOwnQuestions(ownQuestions.get(i));
        }
    }

    void replaceIntoDbOwnQuestions(Question q) {
        Cursor c = db.rawQuery("SELECT question FROM ownQuestions WHERE question = ?", new String[]{q.getQuestion()});

        try {
            db.beginTransaction();

            // if question doesn't exist -> insert into DB
            if (!c.moveToFirst()) {
                ContentValues cv = setOwnQuestionFields(q, true);
                db.insert("ownQuestions", null, cv);
            } else {// otherwise update question fields in the db
                ContentValues cv = setOwnQuestionFields(q, false);
                db.update("ownQuestions", cv, "question=" + q.getQuestion(), null);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            c.close();
        }
    }

    public ContentValues setOwnQuestionFields(Question q, boolean setDeletedSynced) {
        Question.Answer[] answers = q.getResults();
        ContentValues cv = new ContentValues();

        cv.put("question", q.getQuestion());
        cv.put("text", q.getText());
        cv.put("created", q.getCreated());
        cv.put("end", q.getEnd());


        // In case of no votes
        if (answers.length == 0) {
            cv.put("yes", 0);
            cv.put("no", 0);
            // In case of one vote -> get whether it is yes or no
        } else if (answers.length == 1) {
            if (answers[0].getAnswer().equals("yes")) {
                cv.put("yes", answers[0].getVotes());
                cv.put("no", 0);
            } else {
                cv.put("yes", 0);
                cv.put("no", answers[0].getVotes());
            }
            // In case there are two votes
        } else {
            if (answers[0].getAnswer().equals("yes")) {
                cv.put("yes", answers[0].getVotes());
            } else {
                cv.put("no", answers[0].getVotes());
            }

            if (answers[1].getAnswer().equals("yes")) {
                cv.put("yes", answers[1].getVotes());
            } else {
                cv.put("no", answers[1].getVotes());
            }
        }

        if (setDeletedSynced) {
            cv.put("deleted", 0);
            cv.put("synced", 0);
        }

        return cv;

    }


    void replaceIntoDb(Faculty f) {
        // Unfortunately I had to do it like that and not with a Replace Into statment because for some reason the replace statement doesn't work correctly
        Cursor c = db.rawQuery("SELECT * FROM faculties WHERE faculty = ?", new String[]{f.getId()});
        try {
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            if(c.moveToFirst()){
                cv.put("name", f.getName());
                db.update("faculties", cv, "faculty = ?", new String[]{f.getId()});
                db.setTransactionSuccessful();
            }else {
                cv.put("faculty", f.getId());
                cv.put("name", f.getName());
                db.insert("faculties", null, cv);
                db.setTransactionSuccessful();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }
}
