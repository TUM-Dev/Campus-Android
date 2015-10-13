package de.tum.in.tumcampus.models;

import android.content.Context;

import com.squareup.okhttp.CertificatePinner;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public class TUMCabeClient {

    private static final String API_HOSTNAME = Const.API_HOSTNAME;
    private static final String API_BASEURL = "/Api";
    private static final String API_CHAT = "/chat/";
    private static final String API_CHAT_ROOMS = API_CHAT + "rooms/";
    private static final String API_CHAT_MEMBERS = API_CHAT + "members/";
    private static final String API_SESSION = "/session/";
    private static final String API_NEWS = "/news/";
    private static final String API_MENSA = "/mensen/";
    private static final String API_CURRICULA = "/curricula/";
    private static final String API_REPORT = "/report/";
    private static final String API_STATISTICS = "/statistics/";
    private static final String API_CINEMA = "/kino/";
    private static final String API_NOTIFICATIONS = "/notifications/";
    private static final String API_LOCATIONS = "/locations/";


    private static TUMCabeClient instance = null;
    private static Context context = null;
    final RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("X-DEVICE-ID", NetUtils.getDeviceID(TUMCabeClient.context));
        }
    };
    final ErrorHandler errorHandler = new ErrorHandler() {
        @Override
        public Throwable handleError(RetrofitError cause) {
            Throwable t = cause.getCause();
            if (t instanceof SSLPeerUnverifiedException) {
                //TODO show a error message
                //Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
            }
            return t;
        }
    };
    private TUMCabeAPIService service = null;

    private TUMCabeClient() {
        //Pin our known fingerprints, which I retrieved on 28. June 2015
        final CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(API_HOSTNAME, "sha1/eeoui1Gne7kkDN/6HlgoxHkD18s=") //Fakultaet fuer Informatik
                .add(API_HOSTNAME, "sha1/AC508zHZltt8Aa1ZpUg5C9tMNJ8=") //Technische Universitaet Muenchen
                .add(API_HOSTNAME, "sha1/7+NhGLCLRZ1RDbncIhu3ksHeOok=") //DFN-Verein PCA Global
                .add(API_HOSTNAME, "sha1/8GO6fJoWdEqc21TsI81nKY58SU0=") //Deutsche Telekom Root CA 2
                .build();
        final OkHttpClient client = new OkHttpClient();
        client.setCertificatePinner(certificatePinner);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint("https://" + API_HOSTNAME + API_BASEURL)
                .setRequestInterceptor(requestInterceptor)
                .setErrorHandler(errorHandler)
                .build();
        service = restAdapter.create(TUMCabeAPIService.class);
    }

    public static TUMCabeClient getInstance(Context c) {
        TUMCabeClient.context = c.getApplicationContext();
        if (instance == null) {
            instance = new TUMCabeClient();
        }
        return instance;
    }

    public void createRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        verification.setData(chatRoom);
        service.createRoom(verification, cb);
    }

    public ChatRoom createRoom(ChatRoom chatRoom, ChatVerification verification) {
        verification.setData(chatRoom);
        return service.createRoom(verification);
    }

    public ChatRoom getChatRoom(int id) {
        return service.getChatRoom(id);
    }

    public ChatMember createMember(ChatMember chatMember) {
        return service.createMember(chatMember);
    }

    public void leaveChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId(), verification, cb);
    }

    public ChatMessage sendMessage(int roomId, ChatMessage chatMessageCreate) {
        return service.sendMessage(roomId, chatMessageCreate);
    }

    public ChatMessage updateMessage(int roomId, ChatMessage message) {
        return service.updateMessage(roomId, message.getId(), message);
    }

    public ArrayList<ChatMessage> getMessages(int roomId, long messageId, @Body ChatVerification verification) {
        return service.getMessages(roomId, messageId, verification);
    }

    public ArrayList<ChatMessage> getNewMessages(int roomId, @Body ChatVerification verification) {
        return service.getNewMessages(roomId, verification);
    }

    public ChatPublicKey uploadPublicKey(int memberId, ChatPublicKey publicKey) {
        return service.uploadPublicKey(memberId, publicKey);
    }

    public List<ChatRoom> getMemberRooms(int memberId, ChatVerification verification) {
        return service.getMemberRooms(memberId, verification);
    }

    public void getPublicKeysForMember(ChatMember member, Callback<List<ChatPublicKey>> cb) {
        service.getPublicKeysForMember(member.getId(), cb);
    }

    public void uploadRegistrationId(int memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
        service.uploadRegistrationId(memberId, regId, cb);
    }

    public GCMNotification getNotification(int notification) {
        return service.getNotification(notification);
    }

    public void confirm(int notification) {
        service.confirm(notification);
    }

    public List<GCMNotificationLocation> getAllLocations() {
        return service.getAllLocations();
    }

    public GCMNotificationLocation getLocation(int locationId) {
        return service.getLocation(locationId);
    }

    private interface TUMCabeAPIService {

        //Group chat
        @POST(API_CHAT_ROOMS)
        void createRoom(@Body ChatVerification verification, Callback<ChatRoom> cb);

        @POST(API_CHAT_ROOMS)
        ChatRoom createRoom(@Body ChatVerification verification);

        @GET(API_CHAT_ROOMS + "{room}")
        ChatRoom getChatRoom(@Path("room") int id);

        @POST(API_CHAT_ROOMS + "{room}/leave/")
        void leaveChatRoom(@Path("room") int roomId, @Body ChatVerification verification, Callback<ChatRoom> cb);

        //Get/Update single message
        @PUT(API_CHAT_ROOMS + "{room}/message/")
        ChatMessage sendMessage(@Path("room") int roomId, @Body ChatMessage message);

        @PUT(API_CHAT_ROOMS + "{room}/message/{message}/")
        ChatMessage updateMessage(@Path("room") int roomId, @Path("message") int messageId, @Body ChatMessage message);

        //Get all recent messages or older ones
        @POST(API_CHAT_ROOMS + "{room}/messages/{page}/")
        ArrayList<ChatMessage> getMessages(@Path("room") int roomId, @Path("page") long messageId, @Body ChatVerification verification);

        @POST(API_CHAT_ROOMS + "{room}/messages/")
        ArrayList<ChatMessage> getNewMessages(@Path("room") int roomId, @Body ChatVerification verification);

        @POST(API_CHAT_MEMBERS)
        ChatMember createMember(@Body ChatMember chatMember);

        @GET(API_CHAT_MEMBERS + "{lrz_id}/")
        ChatMember getMember(@Path("lrz_id") String lrzId);

        @POST(API_CHAT_MEMBERS + "{memberId}/pubkeys/")
        ChatPublicKey uploadPublicKey(@Path("memberId") int memberId, @Body ChatPublicKey publicKey);

        @POST(API_CHAT_MEMBERS + "{memberId}/rooms/")
        List<ChatRoom> getMemberRooms(@Path("memberId") int memberId, @Body ChatVerification verification);

        @GET(API_CHAT_MEMBERS + "{memberId}/pubkeys/")
        void getPublicKeysForMember(@Path("memberId") int memberId, Callback<List<ChatPublicKey>> cb);

        @POST(API_CHAT_MEMBERS + "{memberId}/registration_ids/add_id")
        void uploadRegistrationId(@Path("memberId") int memberId, @Body ChatRegistrationId regId, Callback<ChatRegistrationId> cb);

        @GET(API_NOTIFICATIONS + "{notification}/")
        GCMNotification getNotification(@Path("notification") int notification);

        @GET(API_NOTIFICATIONS + "confirm/{notification}/")
        String confirm(@Path("notification") int notification);

        //Locations
        @GET(API_LOCATIONS)
        List<GCMNotificationLocation> getAllLocations();

        @GET(API_LOCATIONS + "{locationId}/")
        GCMNotificationLocation getLocation(@Path("locationId") int locationId);
    }
}