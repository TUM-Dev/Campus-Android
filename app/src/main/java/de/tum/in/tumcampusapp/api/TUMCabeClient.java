package de.tum.in.tumcampusapp.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import de.tum.in.tumcampusapp.models.cards.StudyCard;
import de.tum.in.tumcampusapp.models.gcm.GCMNotification;
import de.tum.in.tumcampusapp.models.gcm.GCMNotificationLocation;
import de.tum.in.tumcampusapp.models.tumcabe.BarrierfreeContact;
import de.tum.in.tumcampusapp.models.tumcabe.BarrierfreeMoreInfo;
import de.tum.in.tumcampusapp.models.tumcabe.BugReport;
import de.tum.in.tumcampusapp.models.tumcabe.BuildingToGps;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.ChatPublicKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRegistrationId;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRoom;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumcabe.Curriculum;
import de.tum.in.tumcampusapp.models.tumcabe.DeviceRegister;
import de.tum.in.tumcampusapp.models.tumcabe.DeviceUploadGcmToken;
import de.tum.in.tumcampusapp.models.tumcabe.Faculty;
import de.tum.in.tumcampusapp.models.tumcabe.Feedback;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;
import de.tum.in.tumcampusapp.models.tumcabe.Question;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderMap;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderSchedule;
import de.tum.in.tumcampusapp.models.tumcabe.Statistics;
import de.tum.in.tumcampusapp.models.tumcabe.Success;
import de.tum.in.tumcampusapp.models.tumcabe.TUMCabeStatus;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

public class TUMCabeClient {

    static final String API_HOSTNAME = Const.API_HOSTNAME;
    static final String API_BASEURL = "/Api/";
    static final String API_CHAT = "chat/";
    static final String API_CHAT_ROOMS = API_CHAT + "rooms/";
    static final String API_CHAT_MEMBERS = API_CHAT + "members/";
    //static final String API_SESSION = "session/";
    //static final String API_NEWS = "news/";
    //static final String API_MENSA = "mensen/";
    static final String API_CURRICULA = "curricula/";
    static final String API_REPORT = "report/";
    static final String API_STATISTICS = "statistics/";
    //static final String API_CINEMA = "kino/";
    static final String API_NOTIFICATIONS = "notifications/";
    static final String API_LOCATIONS = "locations/";
    static final String API_DEVICE = "device/";
    static final String API_QUESTION = "question/";
    static final String API_ANSWER_QUESTION = "question/answer/";
    static final String API_OWN_QUESTIONS = "question/my/";
    static final String API_FACULTY = "faculty/";
    static final String API_WIFI_HEATMAP = "wifimap/";
    static final String API_BARRIER_FREE = "barrierfree/";
    static final String API_BARRIER_FREE_CONTACT = "contacts/";
    static final String API_BARRIER_FREE_BUILDINGS_TO_GPS = "getBuilding2Gps/";
    static final String API_BARRIER_FREE_NERBY_FACILITIES = "nerby/";
    static final String API_BARRIER_FREE_LIST_OF_TOILETS = "listOfToilets/";
    static final String API_BARRIER_FREE_LIST_OF_ELEVATORS = "listOfElevators/";
    static final String API_BARRIER_FREE_MORE_INFO = "moreInformation/";
    static final String API_ROOM_FINDER = "roomfinder/room/";
    static final String API_ROOM_FINDER_SEARCH = "search/";
    static final String API_ROOM_FINDER_COORDINATES = "coordinates/";
    static final String API_ROOM_FINDER_AVAILABLE_MAPS = "availableMaps/";
    static final String API_ROOM_FINDER_SCHEDULE = "scheduleById/";
    static final String API_FEEDBACK = "feedback/";
    static final String API_CAFETERIAS = "mensen/";
    static final String API_KINOS = "kino/";
    static final String API_CARD = "cards/";

    private static TUMCabeClient instance;
    private final TUMCabeAPIService service;

    private TUMCabeClient(final Context c) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://" + API_HOSTNAME + API_BASEURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        Gson gson = new GsonBuilder().
                registerTypeAdapter(Date.class,new DateSerializer())
                .create();
        builder.addConverterFactory(GsonConverterFactory.create(gson));
        builder.client(Helper.getOkClient(c));
        service = builder.build()
                         .create(TUMCabeAPIService.class);
    }

    public static synchronized TUMCabeClient getInstance(Context c) {
        if (instance == null) {
            instance = new TUMCabeClient(c.getApplicationContext());
        }
        return instance;
    }

    // Fetches faculty data (facname, id).Relevant for the user to select own major in majorSpinner in WizNavStartActivity
    public List<Faculty> getFaculties() throws IOException {
        return service.getFaculties()
                      .execute()
                      .body();
    }

    // Deletes ownQuestion..Relevant for allowing the user to delete own questions under responses in SurveyActivity
    public void deleteOwnQuestion(int question, Callback<Question> cb) {
        service.deleteOwnQuestion(question)
               .enqueue(cb);
    }

    // Fetches users ownQuestions and responses.Relevant for displaying results on ownQuestion under responses in SurveyActivity
    public List<Question> getOwnQuestions() throws IOException {
        return service.getOwnQuestions()
                      .execute()
                      .body();
    }

    // Submits user's answer on a given question.Gets triggered through in the survey card.
    public void submitAnswer(Question question, Callback<Question> cb) {
        service.answerQuestion(question)
               .enqueue(cb);
    }

    // Fetches openQuestions which are relevant for the surveyCard.
    public List<Question> getOpenQuestions() throws IOException {
        return service.getOpenQuestions()
                      .execute()
                      .body();
    }

    // Submits user's own question. Gets triggered from the SurveyActivity
    public void createQuestion(Question question, Callback<Question> cb) {
        service.createQuestion(question)
               .enqueue(cb);
    }

    public void createRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        verification.setData(chatRoom);
        service.createRoom(verification)
               .enqueue(cb);
    }

    public ChatRoom createRoom(ChatRoom chatRoom, ChatVerification verification) throws IOException {
        verification.setData(chatRoom);
        return service.createRoom(verification)
                      .execute()
                      .body();
    }

    public ChatRoom getChatRoom(int id) throws IOException {
        return service.getChatRoom(id)
                      .execute()
                      .body();
    }

    public ChatMember createMember(ChatMember chatMember) throws IOException {
        return service.createMember(chatMember)
                      .execute()
                      .body();
    }

    public List<StudyCard> getStudyCards() throws IOException {
        return service.getStudyCards().execute().body();
    }

    public StudyCard addStudyCard(StudyCard card, ChatVerification verification) throws IOException {
        verification.setData(card);
        return service.addStudyCard(verification).execute().body();
    }

    public void leaveChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId(), verification)
               .enqueue(cb);
    }

    public Observable<ChatMessage> sendMessage(int roomId, ChatMessage chatMessage) {
        //If the id is zero then its an new entry otherwise try to update it
        Utils.log("Sending: " + chatMessage.getId() + " " + chatMessage.getText());
        if (chatMessage.getId() == 0) {
            return service.sendMessage(roomId, chatMessage);
        }
        return service.updateMessage(roomId, chatMessage.getId(), chatMessage);
    }

    public Observable<List<ChatMessage>> getMessages(int roomId, long messageId, @Body ChatVerification verification) {
        return service.getMessages(roomId, messageId, verification);
    }

    public Observable<List<ChatMessage>> getNewMessages(int roomId, @Body ChatVerification verification) {
        return service.getNewMessages(roomId, verification);
    }

    public List<ChatRoom> getMemberRooms(int memberId, ChatVerification verification) throws IOException {
        return service.getMemberRooms(memberId, verification)
                      .execute()
                      .body();
    }

    public void getPublicKeysForMember(ChatMember member, Callback<List<ChatPublicKey>> cb) {
        service.getPublicKeysForMember(member.getId())
               .enqueue(cb);
    }

    public void uploadRegistrationId(int memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
        service.uploadRegistrationId(memberId, regId)
               .enqueue(cb);
    }

    public GCMNotification getNotification(int notification) throws IOException {
        return service.getNotification(notification)
                      .execute()
                      .body();
    }

    public List<Curriculum> getAllCurriculas() throws IOException {
        return service.getAllCurriculas()
                      .execute()
                      .body();
    }

    public void confirm(int notification) throws IOException {
        service.confirm(notification)
               .execute()
               .body();
    }

    public List<GCMNotificationLocation> getAllLocations() throws IOException {
        return service.getAllLocations()
                      .execute()
                      .body();
    }

    public GCMNotificationLocation getLocation(int locationId) throws IOException {
        return service.getLocation(locationId)
                      .execute()
                      .body();
    }

    public void putBugReport(BugReport r) throws IOException {
        service.putBugReport(r)
               .execute()
               .body();
    }

    public void putStatistics(Statistics s) {
        service.putStatistics(s)
               .enqueue(new Callback<List<String>>() {
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
        service.deviceRegister(verification)
               .enqueue(cb);
    }

    public void deviceUploadGcmToken(DeviceUploadGcmToken verification, Callback<TUMCabeStatus> cb) {
        service.deviceUploadGcmToken(verification)
               .enqueue(cb);
    }

    public void createMeasurements(List<WifiMeasurement> wifiMeasurementList, Callback<TUMCabeStatus> cb) throws IOException {
        service.createMeasurements(wifiMeasurementList)
               .enqueue(cb);
    }

    public List<BarrierfreeContact> getBarrierfreeContactList() throws IOException {
        return service.getBarrierfreeContactList()
                      .execute()
                      .body();
    }

    public List<BarrierfreeMoreInfo> getMoreInfoList() throws IOException {
        return service.getMoreInfoList()
                      .execute()
                      .body();
    }

    public List<RoomFinderRoom> getListOfToilets() throws IOException {
        return service.getListOfToilets()
                      .execute()
                      .body();
    }

    public List<RoomFinderRoom> getListOfElevators() throws IOException {
        return service.getListOfElevators()
                      .execute()
                      .body();
    }

    public List<RoomFinderRoom> getListOfNearbyFacilities(String buildingId) throws IOException {
        return service.getListOfNearbyFacilities(buildingId)
                      .execute()
                      .body();
    }

    public List<BuildingToGps> getBuilding2Gps() throws IOException {
        return service.getBuilding2Gps()
                      .execute()
                      .body();
    }

    public void fetchAvailableMaps(final String archId, Callback<List<RoomFinderMap>> cb) throws IOException {
        service.fetchAvailableMaps(Helper.encodeUrl(archId))
               .enqueue(cb);
    }

    public List<RoomFinderRoom> fetchRooms(String searchStrings) throws IOException {
        return service.fetchRooms(Helper.encodeUrl(searchStrings))
                      .execute()
                      .body();
    }

    public RoomFinderCoordinate fetchCoordinates(String archId)
            throws IOException {
        return service.fetchCoordinates(Helper.encodeUrl(archId))
                      .execute()
                      .body();
    }

    public void fetchCoordinates(String archId, Callback<RoomFinderCoordinate> cb) throws IOException {
        service.fetchCoordinates(Helper.encodeUrl(archId))
               .enqueue(cb);
    }

    public List<RoomFinderSchedule> fetchSchedule(String roomId, String start, String end) throws IOException {
        return service.fetchSchedule(Helper.encodeUrl(roomId),
                                     Helper.encodeUrl(start), Helper.encodeUrl(end))
                      .execute()
                      .body();
    }

    public void sendFeedback(Feedback feedback, String[] imagePaths, Callback<Success> cb) throws IOException {
        service.sendFeedback(feedback).enqueue(cb);
        for(int i = 0; i < imagePaths.length; i++){
            File file = new File(imagePaths[i]);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("feedback_image", i + ".png", reqFile);
            service.sendFeedbackImage(body, i+1, feedback.getId()).enqueue(cb);
        }
    }
    public Observable<List<Cafeteria>> getCafeterias() {
        return service.getCafeterias();
    }


    public Observable<List<Kino>> getKinos(String lastId){
        return service.getKinos(lastId);
    }
}