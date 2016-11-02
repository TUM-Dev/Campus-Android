package de.tum.in.tumcampusapp.api;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.gcm.GCMNotification;
import de.tum.in.tumcampusapp.models.gcm.GCMNotificationLocation;
import de.tum.in.tumcampusapp.models.tumcabe.BugReport;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.ChatPublicKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRegistrationId;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRoom;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumcabe.DeviceRegister;
import de.tum.in.tumcampusapp.models.tumcabe.DeviceUploadGcmToken;
import de.tum.in.tumcampusapp.models.tumcabe.Faculty;
import de.tum.in.tumcampusapp.models.tumcabe.Question;
import de.tum.in.tumcampusapp.models.tumcabe.Statistics;
import de.tum.in.tumcampusapp.models.tumcabe.TUMCabeStatus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public class TUMCabeClient {

    private static final String API_HOSTNAME = Const.API_HOSTNAME;
    private static final String API_BASEURL = "/Api/";
    private static final String API_CHAT = "chat/";
    private static final String API_CHAT_ROOMS = API_CHAT + "rooms/";
    private static final String API_CHAT_MEMBERS = API_CHAT + "members/";
    //private static final String API_SESSION = "session/";
    //private static final String API_NEWS = "news/";
    //private static final String API_MENSA = "mensen/";
    //private static final String API_CURRICULA = "curricula/";
    private static final String API_REPORT = "report/";
    private static final String API_STATISTICS = "statistics/";
    //private static final String API_CINEMA = "kino/";
    private static final String API_NOTIFICATIONS = "notifications/";
    private static final String API_LOCATIONS = "locations/";
    private static final String API_DEVICE = "device/";
    private static final String API_QUESTION = "question/";
    private static final String API_ANSWER_QUESTION = "question/answer/";
    private static final String API_OWN_QUESTIONS = "question/my/";
    private static final String API_FACULTY = "faculty/";


    private static TUMCabeClient instance;
    private final TUMCabeAPIService service;

    private TUMCabeClient(final Context c) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://" + API_HOSTNAME + API_BASEURL)
                .addConverterFactory(GsonConverterFactory.create());

        builder.client(Helper.getOkClient(c));
        service = builder.build().create(TUMCabeAPIService.class);

        /*
        TODO port the error handler to Retrofit 2
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public Throwable handleError(RetrofitError cause) {
                Throwable t = cause.getCause();
                if (t instanceof SSLPeerUnverifiedException) {
                    //TODO show a error message
                    //Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
                }

                //Return the same cause, so it can be handled by other activities
                return cause;
            }
        }; */

    }

    public static synchronized TUMCabeClient getInstance(Context c) {
        if (instance == null) {
            instance = new TUMCabeClient(c.getApplicationContext());
        }
        return instance;
    }

    // Fetches faculty data (facname, id).Relevant for the user to select own major in majorSpinner in WizNavStartActivity
    public List<Faculty> getFaculties() throws IOException {
        return service.getFaculties().execute().body();
    }

    // Deletes ownQuestion..Relevant for allowing the user to delete own questions under responses in SurveyActivity
    public void deleteOwnQuestion(int question, Callback<Question> cb) {
        service.deleteOwnQuestion(question).enqueue(cb);
    }

    // Fetches users ownQuestions and responses.Relevant for displaying results on ownQuestion under responses in SurveyActivity
    public List<Question> getOwnQuestions() throws IOException {
        return service.getOwnQuestions().execute().body();
    }

    // Submits user's answer on a given question.Gets triggered through in the survey card.
    public void submitAnswer(Question question, Callback<Question> cb) {
        service.answerQuestion(question).enqueue(cb);
    }

    // Fetches openQuestions which are relevant for the surveyCard.
    public List<Question> getOpenQuestions() throws IOException {
        return service.getOpenQuestions().execute().body();
    }

    // Submits user's own question. Gets triggered from the SurveyActivity
    public void createQuestion(Question question, Callback<Question> cb) {
        service.createQuestion(question).enqueue(cb);
    }

    public void createRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        verification.setData(chatRoom);
        service.createRoom(verification).enqueue(cb);
    }

    public ChatRoom createRoom(ChatRoom chatRoom, ChatVerification verification) throws IOException {
        verification.setData(chatRoom);
        return service.createRoom(verification).execute().body();
    }

    public ChatRoom getChatRoom(int id) throws IOException {
        return service.getChatRoom(id).execute().body();
    }

    public ChatMember createMember(ChatMember chatMember) throws IOException {
        return service.createMember(chatMember).execute().body();
    }

    public void leaveChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId(), verification).enqueue(cb);
    }

    public ChatMessage sendMessage(int roomId, ChatMessage chatMessageCreate) throws IOException {
        return service.sendMessage(roomId, chatMessageCreate).execute().body();
    }

    public ChatMessage updateMessage(int roomId, ChatMessage message) throws IOException {
        return service.updateMessage(roomId, message.getId(), message).execute().body();
    }

    public List<ChatMessage> getMessages(int roomId, long messageId, @Body ChatVerification verification) throws IOException {
        return service.getMessages(roomId, messageId, verification).execute().body();
    }

    public List<ChatMessage> getNewMessages(int roomId, @Body ChatVerification verification) throws IOException {
        return service.getNewMessages(roomId, verification).execute().body();
    }

    public List<ChatRoom> getMemberRooms(int memberId, ChatVerification verification) throws IOException {
        return service.getMemberRooms(memberId, verification).execute().body();
    }

    public void getPublicKeysForMember(ChatMember member, Callback<List<ChatPublicKey>> cb) {
        service.getPublicKeysForMember(member.getId()).enqueue(cb);
    }

    public void uploadRegistrationId(int memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
        service.uploadRegistrationId(memberId, regId).enqueue(cb);
    }

    public GCMNotification getNotification(int notification) throws IOException {
        return service.getNotification(notification).execute().body();
    }

    public void confirm(int notification) throws IOException {
        service.confirm(notification).execute().body();
    }

    public List<GCMNotificationLocation> getAllLocations() throws IOException {
        return service.getAllLocations().execute().body();
    }

    public GCMNotificationLocation getLocation(int locationId) throws IOException {
        return service.getLocation(locationId).execute().body();
    }

    public void putBugReport(BugReport r) throws IOException {
        service.putBugReport(r).execute().body();
    }

    public void putStatistics(Statistics s) {
        service.putStatistics(s).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                //We don't care about any responses
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                //Or if this fails
            }
        });

    }

    public void deviceRegister(DeviceRegister verification, Callback<TUMCabeStatus> cb) {
        service.deviceRegister(verification).enqueue(cb);
    }

    public void deviceUploadGcmToken(DeviceUploadGcmToken verification, Callback<TUMCabeStatus> cb) {
        service.deviceUploadGcmToken(verification).enqueue(cb);
    }

    private interface TUMCabeAPIService {

        @GET(API_FACULTY)
        Call<List<Faculty>> getFaculties();

        @DELETE(API_QUESTION + "{question}")
        Call<Question> deleteOwnQuestion(@Path("question") int question);

        @GET(API_OWN_QUESTIONS)
        Call<List<Question>> getOwnQuestions();

        @POST(API_ANSWER_QUESTION)
        Call<Question> answerQuestion(@Body Question question);

        //Questions
        @POST(API_QUESTION)
        Call<Question> createQuestion(@Body Question question);

        @GET(API_QUESTION)
        Call<List<Question>> getOpenQuestions();

        //Group chat
        @POST(API_CHAT_ROOMS)
        Call<ChatRoom> createRoom(@Body ChatVerification verification);

        @GET(API_CHAT_ROOMS + "{room}")
        Call<ChatRoom> getChatRoom(@Path("room") int id);

        @POST(API_CHAT_ROOMS + "{room}/leave/")
        Call<ChatRoom> leaveChatRoom(@Path("room") int roomId, @Body ChatVerification verification);

        //Get/Update single message
        @PUT(API_CHAT_ROOMS + "{room}/message/")
        Call<ChatMessage> sendMessage(@Path("room") int roomId, @Body ChatMessage message);

        @PUT(API_CHAT_ROOMS + "{room}/message/{message}/")
        Call<ChatMessage> updateMessage(@Path("room") int roomId, @Path("message") int messageId, @Body ChatMessage message);

        //Get all recent messages or older ones
        @POST(API_CHAT_ROOMS + "{room}/messages/{page}/")
        Call<List<ChatMessage>> getMessages(@Path("room") int roomId, @Path("page") long messageId, @Body ChatVerification verification);

        @POST(API_CHAT_ROOMS + "{room}/messages/")
        Call<List<ChatMessage>> getNewMessages(@Path("room") int roomId, @Body ChatVerification verification);

        @POST(API_CHAT_MEMBERS)
        Call<ChatMember> createMember(@Body ChatMember chatMember);

        @GET(API_CHAT_MEMBERS + "{lrz_id}/")
        Call<ChatMember> getMember(@Path("lrz_id") String lrzId);

        @POST(API_CHAT_MEMBERS + "{memberId}/rooms/")
        Call<List<ChatRoom>> getMemberRooms(@Path("memberId") int memberId, @Body ChatVerification verification);

        @GET(API_CHAT_MEMBERS + "{memberId}/pubkeys/")
        Call<List<ChatPublicKey>> getPublicKeysForMember(@Path("memberId") int memberId);

        @POST(API_CHAT_MEMBERS + "{memberId}/registration_ids/add_id")
        Call<ChatRegistrationId> uploadRegistrationId(@Path("memberId") int memberId, @Body ChatRegistrationId regId);

        @GET(API_NOTIFICATIONS + "{notification}/")
        Call<GCMNotification> getNotification(@Path("notification") int notification);

        @GET(API_NOTIFICATIONS + "confirm/{notification}/")
        Call<String> confirm(@Path("notification") int notification);

        //Locations
        @GET(API_LOCATIONS)
        Call<List<GCMNotificationLocation>> getAllLocations();

        @GET(API_LOCATIONS + "{locationId}/")
        Call<GCMNotificationLocation> getLocation(@Path("locationId") int locationId);

        //Bug Reports
        @PUT(API_REPORT)
        Call<List<String>> putBugReport(@Body BugReport r);

        //Statistics
        @PUT(API_STATISTICS)
        Call<List<String>> putStatistics(@Body Statistics r);

        //Device
        @POST(API_DEVICE + "register/")
        Call<TUMCabeStatus> deviceRegister(@Body DeviceRegister verification);

        @POST(API_DEVICE + "addGcmToken/")
        Call<TUMCabeStatus> deviceUploadGcmToken(@Body DeviceUploadGcmToken verification);

    }
}