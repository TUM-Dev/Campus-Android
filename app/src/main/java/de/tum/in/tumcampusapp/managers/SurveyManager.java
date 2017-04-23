package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.SurveyCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.entities.Faculty;
import de.tum.in.tumcampusapp.entities.Faculty_;
import de.tum.in.tumcampusapp.entities.OpenQuestion;
import de.tum.in.tumcampusapp.entities.OpenQuestion_;
import de.tum.in.tumcampusapp.entities.OwnQuestion;
import de.tum.in.tumcampusapp.entities.OwnQuestion_;
import de.tum.in.tumcampusapp.entities.TcaBoxes;
import de.tum.in.tumcampusapp.models.tumcabe.Question;
import io.objectbox.Box;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SurveyManager for handling database access and downloading external information via TUMCabeClient
 */
public class SurveyManager extends AbstractManager implements Card.ProvidesCard {

    private Box<Faculty> facultyBox;
    private Box<OwnQuestion> ownBox;
    private Box<OpenQuestion> openBox;

    /**
     * Constructor for creating tables if needed
     *
     * @param context
     */
    public SurveyManager(Context context) {
        super(context);

        facultyBox = TcaBoxes.getBoxStore().boxFor(Faculty.class);
        ownBox = TcaBoxes.getBoxStore().boxFor(OwnQuestion.class);
        openBox = TcaBoxes.getBoxStore().boxFor(OpenQuestion.class);
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

        List<OpenQuestion> e = this.getUnansweredQuestions();
        if (e != null) {
            SurveyCard card = new SurveyCard(context);
            card.setQuestion(e);
            card.apply();
        }
    }

    public List<OpenQuestion> getUnansweredQuestions() {
        return openBox.query().equal(OpenQuestion_.answered, 0).build().find();
    }

    /**
     * 1. Fetches openQuestions from server for the surveyCard
     * 2. deletes flagged questions
     * 3. filters the openQuestions according to the major of the user
     * 3. save the questions in the db
     */
    public void downLoadOpenQuestions() {
        List<Question> openQuestions = new ArrayList<>();
        try {
            openQuestions = TUMCabeClient.getInstance(mContext).getOpenQuestions();
        } catch (IOException e) {
            Utils.log(e);
        }

        deleteFlaggedQuestions(openQuestions);

        // filters the questions relevant for the user
        for (int i = 0; i < openQuestions.size(); i++) {
            List<String> openQuestionFaculties = Arrays.asList(openQuestions.get(i).getFaculties());
            String userMajor = Utils.getInternalSettingString(mContext, "user_major", "");

            // Incase  the user selected the major upon app start, then save the major related questions. Otherwise save all questions
            if ("0".equals(userMajor) || openQuestionFaculties.contains(userMajor)) {
                replaceIntoDBOpenQuestions(openQuestions.get(i));
            }
        }
    }

    /**
     * Question in local database tthat no longer exist in the fetched openQuestions from the
     * Server are considered to be flagged and thus get deleted
     *
     * @param fetchedQuestions
     */
    void deleteFlaggedQuestions(List<Question> fetchedQuestions) {
        // get the ids of all fetched openQuestions
        List<Integer> existingQuestions = new ArrayList<>();
        for (Question e : fetchedQuestions) {
            existingQuestions.add(e.getQuestion());
        }

        // get all already existing openQuestions in db
        List<OpenQuestion> all = openBox.getAll();
        for (OpenQuestion e : all) {
            if (!existingQuestions.contains(e.getQuestion())) {
                openBox.remove(e);
            }
        }
    }

    /**
     * Inserts openQuestion in db, if it didn't exist before
     *
     * @param q
     */
    void replaceIntoDBOpenQuestions(Question q) {
        OpenQuestion e = openBox.query().equal(OpenQuestion_.question, q.getQuestion()).build().findFirst();

        // if question doesn't exist, then insert it
        if (e == null) {
            e = new OpenQuestion(q.getQuestion(), q.getText(), DateUtils.parseSqlDateTime(q.getCreated()), DateUtils.parseSqlDateTime(q.getEnd()));
        } else { //Otherwise update
            e.setCreated(DateUtils.parseSqlDateTime(q.getCreated()));
            e.setEnd(DateUtils.parseSqlDateTime(q.getEnd()));
        }
        openBox.put(e);
    }

    /**
     * Used for:
     * 1. Showing faculties in wizNavStartActivity for user selection
     * 2, Showing targetFaculties for selection upon submitting a question
     * 3. Matching the faculties Names to numbers before submiting the question(s) to server
     *
     * @return all faculties (id and name)
     */
    public List<Faculty> getAllFaculties() {
        return facultyBox.getAll();
    }


    /**
     * Collects relevant ownQuestions to be shown in the responses tab in the surveyActivity. A question is relevant when:
     * 1. Is not deleted
     * 2. its end date is still in the future
     *
     * @return relevant ownQuestions
     */
    public List<OwnQuestion> getOwnQuestions() {
        return ownBox.query().equal(OwnQuestion_.deleted, 0).greater(OwnQuestion_.end, (new Date()).getTime()).build().find();
    }

    public OwnQuestion getOwnQuestion(Integer question) {
        return ownBox.query().equal(OwnQuestion_.question, question).build().findFirst();
    }

    public OpenQuestion getOpenQuestion(Integer question) {
        return openBox.query().equal(OpenQuestion_.question, question).build().findFirst();
    }

    /**
     * Handles deleting ownQuestions that are shown in the response tab in SurveyActivity
     *
     * @param question: QuestionID
     */
    public void deleteOwnQuestion(int question) {
        TUMCabeClient.getInstance(mContext).deleteOwnQuestion(question, new Callback<Question>() {
            @Override
            public void onResponse(Call<Question> call, Response<Question> response) {
                Utils.log("TUMCabeClient_delete_question_successeed");
            }

            @Override
            public void onFailure(Call<Question> call, Throwable t) {
                Utils.log(t, "TUMCabeClient_delete_question_failed. ");
            }
        });

        OwnQuestion e = getOwnQuestion(question);
        e.setDeleted(true);
        ownBox.put(e);
    }

    /**
     * 1. Updates the answerID field in local db of a given answered Question in Survey Card
     * 2. Sync the answer to tje server
     *
     * @param question: answered Question to be updated
     * @param answer:   yes = 1 || no = 2 || flag = -1 || skip = 3
     */
    public void updateQuestion(OpenQuestion question, int answer) {
        question.setAnswered(true);
        if (answer == 3) { //Do not sync skipped questions later
            question.setSynced(true);
        } else {
            question.setAnswer(answer);
        }
        openBox.put(question);

        //Trigger sync if we are currently connected
        if (NetUtils.isConnected(mContext)) {
            syncOpenQuestionsTable();
        }

    }

    /**
     * Syncs answered but not yet synced responses with server
     */
    public void syncOpenQuestionsTable() {
        List<OpenQuestion> toSync = openBox.query().equal(OpenQuestion_.synced, "false").equal(OpenQuestion_.answered, "true").build().find();

        for (OpenQuestion e : toSync) {
            try {
                Question answeredQuestion = new Question(e.getQuestion(), e.getAnswer());

                // Submit Answer to Serve
                TUMCabeClient.getInstance(mContext).submitAnswer(answeredQuestion, new Callback<Question>() {
                    @Override
                    public void onResponse(Call<Question> call, Response<Question> response) {
                        Utils.log("Test_resp_submitQues Succeeded: " + response.body());
                    }

                    @Override
                    public void onFailure(Call<Question> call, Throwable t) {
                        Utils.log(t, "Test_resp_submitQues Failure");
                    }
                });

                e.setSynced(true);
                openBox.put(e);
            } catch (Exception ex) {
                Utils.log(ex.toString());
            }
        }
    }

    /**
     * Used to determine whether the user is allowed to create question(s) in a given week and if yes then how many
     *
     * @param d
     * @return questions created since a given date
     */
    public List<OwnQuestion> ownQuestionsSince(DateTime d) {
        return ownBox.query().greater(OwnQuestion_.created, d.toDate().getTime()).build().find();
    }

    /**
     * Used to map selected target faculty names to faculty ids for the question(s) to be submitted
     *
     * @param name
     * @return faculty ID for a given faculty name
     */
    public Faculty getFaculty(String name) {
        return facultyBox.query().equal(Faculty_.name, name).build().findFirst();
    }

    public Faculty getFaculty(Integer faculty) {
        return facultyBox.query().equal(Faculty_.faculty, faculty).build().findFirst();
    }

    /**
     * Fetches the facultyData from the server and saves it in the local db
     */
    public void downloadFacultiesFromExternal() {
        SyncManager sync = new SyncManager(mContext);
        if (!sync.needSync(this, CacheManager.VALIDITY_TEN_DAYS)) {
            return;
        }

        List<Faculty> faculties;
        try {
            faculties = TUMCabeClient.getInstance(mContext).getFaculties();
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        if (faculties == null) {
            Utils.logv("No faculties received...");
            return;
        }

        facultyBox.put(faculties);
        sync.replaceIntoDb(this);
    }


    /**
     * Downloads ownQuestions from server via TUMCabeClient and
     * saves them in local db if they don't exist
     */
    public void downLoadOwnQuestions() {
        List<Question> ownQuestions = new ArrayList<>();
        try {
            ownQuestions = TUMCabeClient.getInstance(mContext).getOwnQuestions();
        } catch (IOException e) {
            Utils.log(e);
        }
        if (ownQuestions.isEmpty()) {
            return;
        }
        for (Question e : ownQuestions) {
            replaceIntoDbOwnQuestions(e);
        }
    }

    /**
     * Help function for downLoadOwnQuestions to write questions in db
     *
     * @param q
     */
    void replaceIntoDbOwnQuestions(Question q) {
        OwnQuestion e = getOwnQuestion(q.getQuestion());
        if (e == null) {
            e = new OwnQuestion();
            e.setQuestion(q.getQuestion());
        }

        e.setText(q.getText());
        e.setCreated(DateUtils.parseSqlDateTime(q.getCreated()));
        e.setEnd(DateUtils.parseSqlDateTime(q.getEnd()));

        List<String> thisFacs = Arrays.asList(q.getFaculties());
        for (Faculty fac : getAllFaculties()) {
            if (thisFacs.contains("" + fac.getFaculty())) {
                e.targetFac.add(fac);
            }
        }

        e.setYes(q.getVotesForAnswer("yes"));
        e.setNo(q.getVotesForAnswer("no"));
        ownBox.put(e);
    }

}
