package de.tum.in.tumcampusapp.api.app;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.model.DeviceRegister;
import de.tum.in.tumcampusapp.api.app.model.DeviceUploadFcmToken;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.FeedbackResult;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotification;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotificationLocation;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierFreeContact;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierFreeMoreInfo;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsAlert;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

/**
 * Proxy class for Retrofit client to our API hosted @app.tum.de
 */
public final class TUMCabeClient {

    static final String API_MEMBERS = "members/";
    static final String API_NOTIFICATIONS = "notifications/";
    static final String API_LOCATIONS = "locations/";
    static final String API_DEVICE = "device/";
    static final String API_BARRIER_FREE = "barrierfree/";
    static final String API_BARRIER_FREE_CONTACT = "contacts/";
    static final String API_BARRIER_FREE_MORE_INFO = "moreInformation/";
    static final String API_ROOM_FINDER = "roomfinder/room/";
    static final String API_ROOM_FINDER_COORDINATES = "coordinates/";
    static final String API_ROOM_FINDER_AVAILABLE_MAPS = "availableMaps/";
    static final String API_ROOM_FINDER_SCHEDULE = "scheduleById/";
    static final String API_FEEDBACK = "feedback/";
    static final String API_CAFETERIAS = "mensen/";
    static final String API_KINOS = "kino/";
    static final String API_NEWS = "news/";
    static final String API_EVENTS = "event/";
    static final String API_TICKET = "ticket/";
    static final String API_STUDY_ROOMS = "studyroom/list";
    private static final String API_HOSTNAME = Const.API_HOSTNAME;
    private static final String API_BASEURL = "/Api/";
    private static final String API_CHAT = "chat/";
    static final String API_CHAT_ROOMS = API_CHAT + "rooms/";
    static final String API_CHAT_MEMBERS = API_CHAT + "members/";
    static final String API_OPENING_HOURS = "openingtimes/";

    private static TUMCabeClient instance;
    private final TUMCabeAPIService service;

    private TUMCabeClient(final Context c) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateSerializer())
                .create();

        service = new Retrofit.Builder()
                .baseUrl("https://" + API_HOSTNAME + API_BASEURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ApiHelper.getOkHttpClient(c))
                .build()
                .create(TUMCabeAPIService.class);
    }

    public static synchronized TUMCabeClient getInstance(Context c) {
        if (instance == null) {
            instance = new TUMCabeClient(c.getApplicationContext());
        }
        return instance;
    }

    private static TUMCabeVerification getVerification(Context context, @Nullable Object data) throws NoPrivateKey {
        TUMCabeVerification verification =
                TUMCabeVerification.create(context, data);
        if (verification == null) {
            throw new NoPrivateKey();
        }

        return verification;
    }

    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public ChatMember createMember(ChatMember chatMember) throws IOException {
        return service.createMember(chatMember)
                .execute()
                .body();
    }

    Observable<TUMCabeStatus> uploadObfuscatedIds(String lrzId, ObfuscatedIdsUpload ids) {
        return service.uploadObfuscatedIds(lrzId, ids);
    }

    public FcmNotification getNotification(int notification) throws IOException {
        return service.getNotification(notification)
                .execute()
                .body();
    }

    public void confirm(int notification) throws IOException {
        service.confirm(notification)
                .execute();
    }

    public FcmNotificationLocation getLocation(int locationId) throws IOException {
        return service.getLocation(locationId)
                .execute()
                .body();
    }

    void deviceRegister(DeviceRegister verification, Callback<TUMCabeStatus> cb) {
        service.deviceRegister(verification)
                .enqueue(cb);
    }

    @Nullable
    public TUMCabeStatus verifyKey() {
        try {
            return service.verifyKey().execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public void deviceUploadGcmToken(DeviceUploadFcmToken verification, Callback<TUMCabeStatus> cb) {
        service.deviceUploadGcmToken(verification)
                .enqueue(cb);
    }

    @Nullable
    public UploadStatus getUploadStatus(String lrzId) {
        try {
            return service.getUploadStatus(lrzId).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public List<BarrierFreeContact> getBarrierfreeContactList() throws IOException {
        return service.getBarrierfreeContactList()
                .execute()
                .body();
    }

    public List<BarrierFreeMoreInfo> getMoreInfoList() throws IOException {
        return service.getMoreInfoList()
                .execute()
                .body();
    }

    public Call<FeedbackResult> sendFeedback(Feedback feedback) {
        return service.sendFeedback(feedback);
    }

    public List<Call<FeedbackResult>> sendFeedbackImages(Feedback feedback, String[] imagePaths) {
        List<Call<FeedbackResult>> calls = new ArrayList<>();
        for (int i = 0; i < imagePaths.length; i++) {
            File file = new File(imagePaths[i]);
            RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("feedback_image", i + ".png", reqFile);

            Call<FeedbackResult> call = service.sendFeedbackImage(body, i + 1, feedback.getId());
            calls.add(call);
        }
        return calls;
    }


    public Observable<List<Cafeteria>> getCafeterias() {
        return service.getCafeterias();
    }

    public Flowable<List<Kino>> getKinos(String lastId) {
        return service.getKinos(lastId);
    }

    public List<News> getNews(String lastNewsId) throws IOException {
        return service.getNews(lastNewsId)
                .execute()
                .body();
    }

    public List<NewsSources> getNewsSources() throws IOException {
        return service.getNewsSources()
                .execute()
                .body();
    }

    public Observable<NewsAlert> getNewsAlert() {
        return service.getNewsAlert();
    }

    public Call<List<StudyRoomGroup>> getStudyRoomGroups() {
        return service.getStudyRoomGroups();
    }

    // TICKET SALE

    // Getting event information
    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public Observable<List<Event>> fetchEvents() {
        return service.getEvents();
    }

    // Getting ticket information

    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public Observable<List<Ticket>> fetchTickets(Context context) throws NoPrivateKey {
        TUMCabeVerification verification = getVerification(context, null);
        return service.getTickets(verification);
    }

    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public Call<Ticket> fetchTicket(Context context, int ticketID) throws NoPrivateKey {
        TUMCabeVerification verification = getVerification(context, null);
        return service.getTicket(ticketID, verification);
    }

    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public Observable<List<TicketType>> fetchTicketTypes(int eventID) {
        return service.getTicketTypes(eventID);
    }

    // Ticket reservation

    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public void reserveTicket(TUMCabeVerification verification,
                              Callback<TicketReservationResponse> cb) {
        service.reserveTicket(verification).enqueue(cb);
    }

    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    public Single<List<TicketStatus>> fetchTicketStats(int event) {
        return service.getTicketStats(event);
    }

    public List<Location> fetchOpeningHours(String language) throws IOException {
        return service.getOpeningHours(language)
                      .execute()
                      .body();
    }

}
