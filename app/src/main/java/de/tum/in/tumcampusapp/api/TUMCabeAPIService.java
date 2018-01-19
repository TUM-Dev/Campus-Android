package de.tum.in.tumcampusapp.api;

import java.util.List;

import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
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
import de.tum.in.tumcampusapp.models.tumcabe.Kino;
import de.tum.in.tumcampusapp.models.tumcabe.Question;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderMap;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderSchedule;
import de.tum.in.tumcampusapp.models.tumcabe.Statistics;
import de.tum.in.tumcampusapp.models.tumcabe.TUMCabeStatus;
import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_ANSWER_QUESTION;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE_BUILDINGS_TO_GPS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE_CONTACT;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE_LIST_OF_ELEVATORS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE_LIST_OF_TOILETS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE_MORE_INFO;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_BARRIER_FREE_NERBY_FACILITIES;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_CAFETERIAS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_CHAT_MEMBERS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_CHAT_ROOMS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_CURRICULA;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_DEVICE;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_FACULTY;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_KINOS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_LOCATIONS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_NOTIFICATIONS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_OWN_QUESTIONS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_QUESTION;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_REPORT;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_ROOM_FINDER;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_ROOM_FINDER_AVAILABLE_MAPS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_ROOM_FINDER_COORDINATES;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_ROOM_FINDER_SCHEDULE;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_ROOM_FINDER_SEARCH;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_STATISTICS;
import static de.tum.in.tumcampusapp.api.TUMCabeClient.API_WIFI_HEATMAP;

public interface TUMCabeAPIService {

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

    //Curricula
    @GET(API_CURRICULA)
    Call<List<Curriculum>> getAllCurriculas();

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

    //WifiHeatmap
    @POST(API_WIFI_HEATMAP + "create_measurements/")
    Call<TUMCabeStatus> createMeasurements(@Body List<WifiMeasurement> wifiMeasurementList);

    // Barrier free contacts
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_CONTACT)
    Call<List<BarrierfreeContact>> getBarrierfreeContactList();

    // Barrier free More Info
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_MORE_INFO)
    Call<List<BarrierfreeMoreInfo>> getMoreInfoList();

    // Barrier free toilets list
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_LIST_OF_TOILETS)
    Call<List<RoomFinderRoom>> getListOfToilets();

    // Barrier free elevator list
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_LIST_OF_ELEVATORS)
    Call<List<RoomFinderRoom>> getListOfElevators();

    // Barrier free nearby list
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_NERBY_FACILITIES + "{buildingId}/")
    Call<List<RoomFinderRoom>> getListOfNearbyFacilities(@Path("buildingId") String buildingId);

    // building to gps information
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_BUILDINGS_TO_GPS)
    Call<List<BuildingToGps>> getBuilding2Gps();

    //RoomFinder maps
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_AVAILABLE_MAPS + "{archId}")
    Call<List<RoomFinderMap>> fetchAvailableMaps(@Path("archId") String archId);

    //RoomFinder maps
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_SEARCH + "{searchStrings}")
    Call<List<RoomFinderRoom>> fetchRooms(@Path("searchStrings") String searchStrings);

    //RoomFinder cordinates
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_COORDINATES + "{archId}")
    Call<RoomFinderCoordinate> fetchCoordinates(@Path("archId") String archId);

    //RoomFinder schedule
    @GET(API_ROOM_FINDER + API_ROOM_FINDER_SCHEDULE + "{roomId}" + "/" + "{start}" + "/" + "{end}")
    Call<List<RoomFinderSchedule>> fetchSchedule(@Path("roomId") String archId,
                                                 @Path("start") String start, @Path("end") String end);

    @GET(API_CAFETERIAS)
    Observable<List<Cafeteria>> getCafeterias();

    @GET(API_KINOS+"{lastId}")
    Observable<List<Kino>> getKinos(@Path("lastId") String lastId);

}