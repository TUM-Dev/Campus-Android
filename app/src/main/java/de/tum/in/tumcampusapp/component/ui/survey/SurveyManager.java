package de.tum.in.tumcampusapp.component.ui.survey;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.tumui.person.FacultyDao;
import de.tum.in.tumcampusapp.component.tumui.person.model.Faculty;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.survey.model.OpenQuestions;
import de.tum.in.tumcampusapp.component.ui.survey.model.OwnQuestions;
import de.tum.in.tumcampusapp.component.ui.survey.model.Question;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SurveyManager for handling database access and downloading external information via TUMCabeClient
 */
public class SurveyManager implements Card.ProvidesCard {

    private final FacultyDao facultyDao;
    private final OpenQuestionsDao openQuestionsDao;
    private final OwnQuestionsDao ownQuestionsDao;
    private final Context mContext;

    /**
     * Constructor for creating tables if needed
     *
     * @param context
     */
    public SurveyManager(Context context) {
        mContext = context;
        facultyDao = TcaDb.getInstance(context)
                          .facultyDao();
        openQuestionsDao = TcaDb.getInstance(context)
                                .openQuestionsDao();
        ownQuestionsDao = TcaDb.getInstance(context)
                               .ownQuestionsDao();
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
        List<OpenQuestions> unansweredQuestions = getUnansweredQuestionsSince(DateUtils.getDateTimeString(new Date()));
        if (!unansweredQuestions.isEmpty()) {
            SurveyCard card = new SurveyCard(context);
            card.setQuestions(unansweredQuestions);
            card.apply();
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
        for (Question openQuestion : openQuestions) {
            List<String> openQuestionFaculties = Arrays.asList(openQuestion.getFacultiesOfOpenQuestions());
            String userMajor = Utils.getInternalSettingString(mContext, "user_major", "");

            // Incase  the user selected the major upon app start, then save the major related questions. Otherwise save all questions
            if ("0".equals(userMajor) || openQuestionFaculties.contains(userMajor)) {
                replaceIntoDBOpenQuestions(openQuestion);
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
        for (Question fetchedOpenedQuestion : fetchedOpenedQuestions) {
            downloadedQuestionsID.add(new Question(fetchedOpenedQuestion.getQuestion()));
        }

        // get all already existing openQuestions in db
        List<OpenQuestions> openQuestions = openQuestionsDao.getAll();
        for (OpenQuestions openQuestion : openQuestions) {
            if (!downloadedQuestionsID.contains(new Question(Integer.toString(openQuestion.getQuestion())))) {
                openQuestionsDao.deleteQuestionById(openQuestion.getQuestion());
            }
        }
    }

    /**
     * Inserts openQuestion in db, if it didn't exist before
     *
     * @param q
     */
    void replaceIntoDBOpenQuestions(Question q) {
        OpenQuestions openQuestion = openQuestionsDao.getQuestionById(Integer.parseInt(q.getQuestion()));

        if (openQuestion != null) {
            openQuestionsDao.insert(new OpenQuestions(Integer.parseInt(q.getQuestion()),
                                                      q.getText(),
                                                      q.getCreated(),
                                                      q.getEnd(),
                                                      0,
                                                      false,
                                                      false));
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
    public List<Faculty> getAllFaculties() {
        return facultyDao.getAll();
    }

    /**
     * Gets relevant questions to be shown in the card: unanswered and their end date is still in the future.
     * Flagged questions get removed from the db @deleteFlaggedQuestions()
     *
     * @param date: is usually today's date
     * @return
     */
    public List<OpenQuestions> getUnansweredQuestionsSince(String date) {
        return openQuestionsDao.getUnansweredQuestions(date);
    }

    /**
     * Collects relevant ownQuestions to be shown in the responses tab in the surveyActivity. A question is relevant when:
     * 1. Is not deleted
     * 2. its end date is still in the future
     *
     * @param date: is usually today's date
     * @return relevant ownQuestions
     */
    public List<OwnQuestions> getMyRelevantOwnQuestionsSince(String date) {
        return ownQuestionsDao.getFromEndDate(date);
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
        ownQuestionsDao.setDeletedById(id);
    }

    /**
     * 1. Updates the answerID field in local db of a given answered Question in Survey Card
     * 2. Sync the answer to tje server
     *
     * @param question:  answered Question to be updated
     * @param answerTag: yes = 1 || no = 2 || flag = -1 || skip = 3
     */
    public void updateQuestion(Question question, int answerTag) {
        openQuestionsDao.insert(new OpenQuestions(Integer.parseInt(question.getQuestion()),
                                                  question.getText(),
                                                  question.getCreated(),
                                                  question.getEnd(),
                                                  answerTag == 3 ? 3 : 0,
                                                  true,
                                                  answerTag != 3));

        //Trigger sync if we are currently connected
        if (NetUtils.isConnected(mContext)) {
            syncOpenQuestionsTable();
        }

    }

    /**
     * Syncs answered but not yet synced responses with server
     */
    public void syncOpenQuestionsTable() {
        List<OpenQuestions> answeredNotSyncedQuestions = openQuestionsDao.getAnsweredNotSynced();
        for (OpenQuestions question : answeredNotSyncedQuestions) {
            Question answeredQuestion = new Question(Integer.toString(question.getQuestion()), question.getAnswerid());

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

            openQuestionsDao.markSynced(question.getQuestion());
        }
    }

    /**
     * Used to determine whether the user is allowed to create question(s) in a given week and if yes then how many
     *
     * @param weekAgo
     * @return questions created since a given date
     */
    public List<String> ownQuestionsSince(String weekAgo) {
        return ownQuestionsDao.getFromCreatedDate(weekAgo);
    }

    /**
     * Used to map selected target faculty names to faculty ids for the question(s) to be submitted
     *
     * @param facultyName
     * @return faculty ID for a given faculty name
     */
    public String getFacultyID(String facultyName) {
        return facultyDao.getFacultyIdByName(facultyName);
    }

    public String getFacultyName(String facultyID) {
        return facultyDao.getFacultyNameById(facultyID);
    }

    /**
     * Fetches the facultyData from the server and saves it in the local db
     */
    public void downloadFacultiesFromExternal() {
        List<Faculty> faculties = new ArrayList<>();
        try {
            faculties = TUMCabeClient.getInstance(mContext)
                                     .getFaculties();
        } catch (IOException e) {
            Utils.log(e);
            return;
        }

        for (Faculty faculty : faculties) {
            replaceIntoDb(faculty);
        }
    }

    /**
     * Help function for downloadFacultiesFromExternal.
     * Inserts a given faculty (id and name) in db except if it doesn't exist, else it updates the raw faculty name given the faculty id
     *
     * @param f: a given faculty
     */
    void replaceIntoDb(Faculty f) {
        facultyDao.insert(f);
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

        for (Question question : ownQuestions) {
            replaceIntoDbOwnQuestions(question);
        }
    }

    /**
     * Help function for downLoadOwnQuestions to write questions in db
     *
     * @param q
     */
    void replaceIntoDbOwnQuestions(Question q) {
        /*
        @PrimaryKey
                        var question: Int = -1,
                        var test: String = "",
                        var targetFac: String = "",
                        var created: String = "",
                        var end: String = "",
                        var yes: Int = -1,
                        var no: Int = -1,
                        var deleted: Boolean = false,
                        var synced: Boolean = false)
         */
        Question.Answer[] answers = q.getResults();
        int yes = 0;
        int no = 0;
        if (answers.length == 0) {
            yes = 0;
            no = 0;
        } else if (answers.length == 1) { // In case of one vote -> get whether it is yes or no
            if (answers[0].getAnswer()
                          .equals("yes")) {
                yes = answers[0].getVotes();
                no = 0;
            } else {
                yes = 0;
                no = answers[0].getVotes();
            }
            // In case there are two votes
        } else {
            if (answers[0].getAnswer()
                          .equals("yes")) {
                yes = answers[0].getVotes();
            } else {
                no = answers[0].getVotes();
            }

            if (answers[1].getAnswer()
                          .equals("yes")) {
                yes = answers[1].getVotes();
            } else {
                no = answers[1].getVotes();
            }
        }
        boolean exists = ownQuestionsDao.getById(Integer.parseInt(q.getQuestion())) != null;
        ownQuestionsDao.insert(new OwnQuestions(Integer.parseInt(q.getQuestion()),
                                                q.getText(),
                                                TextUtils.join(",", q.getFacultiesOfOpenQuestions()),
                                                q.getCreated(),
                                                q.getEnd(),
                                                yes,
                                                no,
                                                exists,
                                                exists
        ));
    }
}
