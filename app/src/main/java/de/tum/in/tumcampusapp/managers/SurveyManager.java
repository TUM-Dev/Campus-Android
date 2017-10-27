package de.tum.in.tumcampusapp.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.SurveyCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.tumcabe.Faculty;
import de.tum.in.tumcampusapp.models.tumcabe.Question;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SurveyManager for handling database access and downloading external information via TUMCabeClient
 */
public class SurveyManager extends AbstractManager implements Card.ProvidesCard {

    /**
     * Constructor for creating tables if needed
     *
     * @param context
     */
    public SurveyManager(Context context) {
        super(context);
        db.execSQL("CREATE TABLE IF NOT EXISTS faculties (faculty INTEGER, name VARCHAR)"); // for facultyData
        db.execSQL("CREATE TABLE IF NOT EXISTS openQuestions (question INTEGER PRIMARY KEY, text VARCHAR, created VARCHAR, end VARCHAR, answerid INTEGER, answered BOOLEAN, synced BOOLEAN)"); // for SurveyCard
        db.execSQL("CREATE TABLE IF NOT EXISTS ownQuestions (question INTEGER PRIMARY KEY, text VARCHAR, targetFac VARCHAR, created VARCHAR, end VARCHAR, yes INTEGER, no INTEGER, deleted BOOLEAN, synced BOOLEAN)"); // for responses on ownQuestions
    }

    /**
     * Collects the unansweredQuestions relevant for the card and then 'applies' the card with these questions
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        if (NetUtils.isConnected(mContext)) {
            downLoadOpenQuestions();
        }
        try (Cursor rows = getUnansweredQuestionsSince(Utils.getDateTimeString(new Date()))) {
            if (rows.moveToFirst()) {
                SurveyCard card = new SurveyCard(context);
                card.setQuestions(rows);
                card.apply();
            }
        }
    }

    /**
     * 1. Fetches openQuestions from server for the surveyCard
     * 2. deletes flagged questions
     * 3. filters the openQuestions according to the major of the user
     * 3. save the questions in the db
     */
    public void downLoadOpenQuestions() {
        List<Question> openQuestions;
        try {
            openQuestions = TUMCabeClient.getInstance(mContext)
                                         .getOpenQuestions();
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        deleteFlaggedQuestions(openQuestions);

        // filters the questions relevant for the user
        for (int i = 0; i < openQuestions.size(); i++) {
            List<String> openQuestionFaculties = Arrays.asList(openQuestions.get(i)
                                                                            .getFacultiesOfOpenQuestions());
            String userMajor = Utils.getInternalSettingString(mContext, "user_major", "");

            // Incase  the user selected the major upon app start, then save the major related questions. Otherwise save all questions
            if ("0".equals(userMajor) || openQuestionFaculties.contains(userMajor)) {
                replaceIntoDBOpenQuestions(openQuestions.get(i));
            }
        }
    }

    /**
     * Question in local database that no longer exist in the fetched openQuestions from the
     * Server are considered to be flagged and thus get deleted
     *
     * @param fetchedOpenedQuestions
     */
    void deleteFlaggedQuestions(List<Question> fetchedOpenedQuestions) {
        List<Question> downloadedQuestionsID = new ArrayList<>(); // get the ids of all fetched openQuestions
        for (int x = 0; x < fetchedOpenedQuestions.size(); x++) {
            downloadedQuestionsID.add(new Question(fetchedOpenedQuestions.get(x)
                                                                         .getQuestion()));
        }

        // get all already existing openQuestions in db
        try (Cursor c = db.rawQuery("SELECT question FROM openQuestions", null)) {
            if (c != null) {
                while (c.moveToNext()) { // iterates on each question in the db
                    // Incase the question from the database is not contained in the list with the downloaded questions, the question gets deleted from db
                    if (!downloadedQuestionsID.contains(new Question(c.getString(c.getColumnIndex("question"))))) {
                        db.delete("openQuestions", "question = ?", new String[]{c.getString(c.getColumnIndex("question"))});
                    }
                }
            }
        }
    }

    /**
     * Inserts openQuestion in db, if it didn't exist before
     *
     * @param q
     */
    void replaceIntoDBOpenQuestions(Question q) {
        try (Cursor c = db.rawQuery("SELECT answerid FROM openQuestions WHERE question = ?", new String[]{q.getQuestion()})) {
            // if question doesn't exist, then insert it
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
                } finally {
                    db.endTransaction();
                }
            }
        }
    }

    /**
     * Used for:
     * 1. Showing faculties in wizNavStartActivity for user selection
     * 2, Showing targetFaculties for selection upon submitting a question
     * 3. Matching the faculties Names to numbers before submiting the question(s) to server
     *
     * @return all faculties (id and name)
     */
    public Cursor getAllFaculties() {
        return db.rawQuery("SELECT * FROM faculties", null);
    }

    /**
     * Gets relevant questions to be shown in the card: unanswered and their end date is still in the future.
     * Flagged questions get removed from the db @deleteFlaggedQuestions()
     *
     * @param date: is usually today's date
     * @return
     */
    public Cursor getUnansweredQuestionsSince(String date) {
        return db.rawQuery("SELECT question, text FROM openQuestions WHERE answered=0 AND end >= '" + date + "'", null);
    }

    /**
     * Collects relevant ownQuestions to be shown in the responses tab in the surveyActivity. A question is relevant when:
     * 1. Is not deleted
     * 2. its end date is still in the future
     *
     * @param date: is usually today's date
     * @return relevant ownQuestions
     */
    public Cursor getMyRelevantOwnQuestionsSince(String date) {
        return db.rawQuery("SELECT * FROM ownQuestions where deleted = 0 AND end >= '" + date + "'", null);
    }

    /**
     * Handles deleting ownQuestions that are shown in the response tab in SurveyActivity
     *
     * @param id: QuestionID
     */
    public void deleteMyOwnQuestion(int id) {
        TUMCabeClient.getInstance(mContext)
                     .deleteOwnQuestion(id, new Callback<Question>() {
                         @Override
                         public void onResponse(Call<Question> call, Response<Question> response) {
                             Utils.log("TUMCabeClient_delete_question_successeed");
                         }

                         @Override
                         public void onFailure(Call<Question> call, Throwable t) {
                             Utils.log(t, "TUMCabeClient_delete_question_failed. ");
                         }
                     });
        db.execSQL("UPDATE ownQuestions SET deleted=1 WHERE question=" + id); // Marks question as deleted in local db
    }

    /**
     * 1. Updates the answerID field in local db of a given answered Question in Survey Card
     * 2. Sync the answer to tje server
     *
     * @param question:  answered Question to be updated
     * @param answerTag: yes = 1 || no = 2 || flag = -1 || skip = 3
     */
    public void updateQuestion(Question question, int answerTag) {
        ContentValues cv = new ContentValues();

        if (answerTag == 3) {
            cv.put("synced", 1);//Do not sync skipped questions later
        } else {
            cv.put("answerid", answerTag);
        }

        // Set as answered independent of the answerTag
        cv.put("answered", 1);

        //Commit update to database
        try {
            db.beginTransaction();
            db.update("openQuestions", cv, "question = ?", new String[]{question.getQuestion()});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        //Trigger sync if we are currently connected
        if (NetUtils.isConnected(mContext)) {
            syncOpenQuestionsTable();
        }

    }

    /**
     * Syncs answered but not yet synced responses with server
     */
    public void syncOpenQuestionsTable() {
        try (Cursor cursor = db.rawQuery("SELECT question, answerid FROM openQuestions WHERE synced=0 AND answered=1", null)) {
            // In case there are answered but not yet synced questions in local db
            while (cursor.moveToNext()) {
                Question answeredQuestion = new Question(cursor.getString(cursor.getColumnIndex("question")), cursor.getInt(cursor.getColumnIndex("answerid")));

                // Submit Answer to Serve
                TUMCabeClient.getInstance(mContext)
                             .submitAnswer(answeredQuestion, new Callback<Question>() {
                                 @Override
                                 public void onResponse(Call<Question> call, Response<Question> response) {
                                     Utils.log("Test_resp_submitQues Succeeded: " + response.body());
                                 }

                                 @Override
                                 public void onFailure(Call<Question> call, Throwable t) {
                                     Utils.log(t, "Test_resp_submitQues Failure");
                                 }
                             });

                // Mark as synced in local db
                ContentValues cv = new ContentValues();
                cv.put("synced", "1");
                db.update("openQuestions", cv, "question = ?", new String[]{cursor.getString(cursor.getColumnIndex("question"))});
            }
        } catch (Exception e) {
            Utils.log(e.toString());
        }
    }

    /**
     * Used to determine whether the user is allowed to create question(s) in a given week and if yes then how many
     *
     * @param weekAgo
     * @return questions created since a given date
     */
    public Cursor ownQuestionsSince(String weekAgo) {
        return db.rawQuery("SELECT created FROM ownQuestions WHERE created >= '" + weekAgo + "'", null);
    }

    /**
     * Used to map selected target faculty names to faculty ids for the question(s) to be submitted
     *
     * @param facultyName
     * @return faculty ID for a given faculty name
     */
    public Cursor getFacultyID(String facultyName) {
        return db.rawQuery("SELECT faculty FROM faculties WHERE name=?", new String[]{facultyName});
    }

    public Cursor getFacultyName(String facultyID) {
        return db.rawQuery("SELECT name FROM faculties WHERE faculty=?", new String[]{facultyID});
    }

    /**
     * Fetches the facultyData from the server and saves it in the local db
     */
    public void downloadFacultiesFromExternal() {
        List<Faculty> faculties;
        try {
            faculties = TUMCabeClient.getInstance(mContext)
                                     .getFaculties();
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        if (faculties == null) {
            Utils.logv("No faculties received...");
            return;
        }

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

    /**
     * Help function for downloadFacultiesFromExternal.
     * Inserts a given faculty (id and name) in db except if it doesn't exist, else it updates the raw faculty name given the faculty id
     *
     * @param f: a given faculty
     */
    void replaceIntoDb(Faculty f) {
        try (Cursor c = db.rawQuery("SELECT * FROM faculties WHERE faculty = ?", new String[]{f.getId()})) {
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            if (c.moveToFirst()) { // if faculty exists, update name
                cv.put("name", f.getName());
                db.update("faculties", cv, "faculty = ?", new String[]{f.getId()});
                db.setTransactionSuccessful();
            } else { // else inserts new faculty
                cv.put("faculty", f.getId());
                cv.put("name", f.getName());
                db.insert("faculties", null, cv);
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Downloads ownQuestions from server via TUMCabeClient and
     * saves them in local db if they don't exist
     */
    public void downLoadOwnQuestions() {
        List<Question> ownQuestions = new ArrayList<>();
        try {
            ownQuestions = TUMCabeClient.getInstance(mContext)
                                        .getOwnQuestions();
        } catch (IOException e) {
            Utils.log(e);
        }
        if (ownQuestions.isEmpty()) {
            return;
        }
        for (int i = 0; i < ownQuestions.size(); i++) {
            replaceIntoDbOwnQuestions(ownQuestions.get(i));
        }
    }

    /**
     * Help function for downLoadOwnQuestions to write questions in db
     *
     * @param q
     */
    void replaceIntoDbOwnQuestions(Question q) {
        try (Cursor c = db.rawQuery("SELECT question FROM ownQuestions WHERE question = ?", new String[]{q.getQuestion()})) {
            db.beginTransaction();

            if (c.moveToFirst()) {// update non-exsisting question fields in the db (false means don't update 'delete' and 'synced' fields
                ContentValues cv = setOwnQuestionFields(q, false);
                db.update("ownQuestions", cv, "question=" + q.getQuestion(), null);
            } else { // if question doesn't exist -> insert into DB
                ContentValues cv = setOwnQuestionFields(q, true);
                db.insert("ownQuestions", null, cv);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();

        }
    }

    /**
     * Help function for replaceIntoDBOwnQuestions
     *
     * @param q:               question
     * @param setDeletedSynced a flag whether fields 'deleted' and 'synced' in db should be synced
     * @return Contentvalues that updates all respective fields of the question in db
     */
    public ContentValues setOwnQuestionFields(Question q, boolean setDeletedSynced) {
        Question.Answer[] answers = q.getResults();
        ContentValues cv = new ContentValues();

        cv.put("question", q.getQuestion());
        cv.put("text", q.getText());
        cv.put("created", q.getCreated());
        cv.put("end", q.getEnd());
        cv.put("targetFac", TextUtils.join(",", q.getFacultiesOfOpenQuestions()));

        // In case of no votes
        if (answers.length == 0) {
            cv.put("yes", 0);
            cv.put("no", 0);
        } else if (answers.length == 1) { // In case of one vote -> get whether it is yes or no
            if (answers[0].getAnswer()
                          .equals("yes")) {
                cv.put("yes", answers[0].getVotes());
                cv.put("no", 0);
            } else {
                cv.put("yes", 0);
                cv.put("no", answers[0].getVotes());
            }
            // In case there are two votes
        } else {
            if (answers[0].getAnswer()
                          .equals("yes")) {
                cv.put("yes", answers[0].getVotes());
            } else {
                cv.put("no", answers[0].getVotes());
            }

            if (answers[1].getAnswer()
                          .equals("yes")) {
                cv.put("yes", answers[1].getVotes());
            } else {
                cv.put("no", answers[1].getVotes());
            }
        }
        if (setDeletedSynced) { // if question is new then set deleted and synced otherwise no
            cv.put("deleted", 0);
            cv.put("synced", 0);
        }
        return cv;
    }
}
