package de.tum.in.tumcampusapp.api.app;

import java.util.List;

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
import de.tum.in.tumcampusapp.component.ui.news.model.NewsAlert;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_CONTACT;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_BARRIER_FREE_MORE_INFO;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_CAFETERIAS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_CHAT_MEMBERS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_DEVICE;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_EVENTS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_FEEDBACK;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_KINOS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_LOCATIONS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_MEMBERS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_NEWS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_NOTIFICATIONS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_OPENING_HOURS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_STUDY_ROOMS;
import static de.tum.in.tumcampusapp.api.app.TUMCabeClient.API_TICKET;

public interface TUMCabeAPIService {
    @POST(API_CHAT_MEMBERS)
    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    Call<ChatMember> createMember(@Body ChatMember chatMember);

    @POST(API_MEMBERS + "uploadIds/{lrzId}/")
    Observable<TUMCabeStatus> uploadObfuscatedIds(@Path("lrzId") String lrzId, @Body ObfuscatedIdsUpload ids);

    @GET(API_NOTIFICATIONS + "{notification}/")
    Call<FcmNotification> getNotification(@Path("notification") int notification);

    @GET(API_NOTIFICATIONS + "confirm/{notification}/")
    Call<String> confirm(@Path("notification") int notification);

    @GET(API_LOCATIONS + "{locationId}/")
    Call<FcmNotificationLocation> getLocation(@Path("locationId") int locationId);

    //Device
    @POST(API_DEVICE + "register/")
    Call<TUMCabeStatus> deviceRegister(@Body DeviceRegister verification);

    @GET(API_DEVICE + "verifyKey/")
    Call<TUMCabeStatus> verifyKey();

    @POST(API_DEVICE + "addGcmToken/")
    Call<TUMCabeStatus> deviceUploadGcmToken(@Body DeviceUploadFcmToken verification);

    @GET(API_DEVICE + "uploaded/{lrzId}")
    Call<UploadStatus> getUploadStatus(@Path("lrzId") String lrzId);

    // Barrier free contacts
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_CONTACT)
    Call<List<BarrierFreeContact>> getBarrierfreeContactList();

    // Barrier free More Info
    @GET(API_BARRIER_FREE + API_BARRIER_FREE_MORE_INFO)
    Call<List<BarrierFreeMoreInfo>> getMoreInfoList();

    @POST(API_FEEDBACK)
    Call<FeedbackResult> sendFeedback(@Body Feedback feedback);

    @Multipart
    @POST(API_FEEDBACK + "{id}/{image}/")
    Call<FeedbackResult> sendFeedbackImage(@Part MultipartBody.Part image, @Path("image") int imageNr, @Path("id") String feedbackId);

    @GET(API_CAFETERIAS)
    Observable<List<Cafeteria>> getCafeterias();

    @GET(API_KINOS + "{lastId}")
    Flowable<List<Kino>> getKinos(@Path("lastId") String lastId);

    @GET(API_NEWS + "alert")
    Observable<NewsAlert> getNewsAlert();

    @GET(API_STUDY_ROOMS)
    Call<List<StudyRoomGroup>> getStudyRoomGroups();

    // TICKET SALE

    // Getting Event information

    @GET(API_EVENTS + "list")
    @Deprecated
        /// This endpoint won't be avaliable in the v2 backend
    Observable<List<Event>> getEvents();

    // Getting Ticket information
    @POST(API_EVENTS + API_TICKET + "my")
    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    Observable<List<Ticket>> getTickets(@Body TUMCabeVerification verification);

    @POST(API_EVENTS + API_TICKET + "{ticketID}")
    @Deprecated
        /// This endpoint won't be avaliable in the v2 backend
    Call<Ticket> getTicket(@Path("ticketID") int ticketID, @Body TUMCabeVerification verification);

    @GET(API_EVENTS + API_TICKET + "type/{eventID}")
    @Deprecated
        /// This endpoint won't be avaliable in the v2 backend
    Observable<List<TicketType>> getTicketTypes(@Path("eventID") int eventID);

    // Ticket reservation
    @POST(API_EVENTS + API_TICKET + "reserve/multiple")
    @Deprecated
    /// This endpoint won't be avaliable in the v2 backend
    Call<TicketReservationResponse> reserveTicket(@Body TUMCabeVerification verification);

    @GET(API_EVENTS + API_TICKET + "status/{event}")
    @Deprecated
        /// This endpoint won't be avaliable in the v2 backend
    Single<List<TicketStatus>> getTicketStats(@Path("event") int event);

    // Opening Hours
    @GET(API_OPENING_HOURS + "{language}")
    Call<List<Location>> getOpeningHours(@Path("language") String language);

}
