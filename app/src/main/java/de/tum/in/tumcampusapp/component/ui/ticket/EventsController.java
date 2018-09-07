package de.tum.in.tumcampusapp.component.ui.ticket;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class is responsible for providing ticket and event data to the activities.
 * For that purpose it handles both server and database accesses.
 */
public class EventsController implements ProvidesCard {

    private final Context context;

    private final EventDao eventDao;
    private final TicketDao ticketDao;
    private final TicketTypeDao ticketTypeDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public EventsController(Context context) {
        this.context = context;
        TcaDb db = TcaDb.getInstance(context);
        eventDao = db.eventDao();
        ticketDao = db.ticketDao();
        ticketTypeDao = db.ticketTypeDao();
    }

    public void downloadFromService() {
        Callback<List<Event>> eventCallback = new Callback<List<Event>>() {

            @Override
            public void onResponse(@NonNull Call<List<Event>> call, Response<List<Event>> response) {
                List<Event> events = response.body();
                if (events == null) {
                    return;
                }
                storeEvents(events);
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull  Throwable t) {
                Utils.log(t);
            }
        };

        Callback<List<Ticket>> ticketCallback = new Callback<List<Ticket>>() {
            @Override
            public void onResponse(@NonNull Call<List<Ticket>> call, Response<List<Ticket>> response) {
                List<Ticket> tickets = response.body();
                if (tickets == null) {
                    return;
                }
                insert(tickets.toArray(new Ticket[0]));
                loadTicketTypesForTickets(tickets);
            }

            @Override
            public void onFailure(@NonNull Call<List<Ticket>> call, @NonNull Throwable t) {
                Utils.log(t);
            }
        };

        getEventsAndTicketsFromServer(eventCallback, ticketCallback);
    }

    public void getEventsAndTicketsFromServer(Callback<List<Event>> eventCallback,
                                              Callback<List<Ticket>> ticketCallback) {
        // Delete all too old items
        eventDao.removePastEvents();

        // Load all events
        TUMCabeClient.getInstance(context).fetchEvents(eventCallback);

        // Load all tickets
        try {
            if (Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class) != null) {
                TUMCabeClient.getInstance(context).fetchTickets(context, ticketCallback);
            }
        } catch (NoPrivateKey e) {
            Utils.log(e);
        }
    }

    private void loadTicketTypesForTickets(Iterable<Ticket> tickets){
        // get ticket type information for all tickets
        for (Ticket ticket : tickets){
            TUMCabeClient.getInstance(context).fetchTicketTypes(ticket.getEventId(),
                    new Callback<List<TicketType>>(){

                        @Override
                        public void onResponse(@NonNull Call<List<TicketType>> call, @NonNull Response<List<TicketType>> response) {
                            List<TicketType> ticketTypes = response.body();
                            if (ticketTypes == null) {
                                return;
                            }
                            // add found ticket types to database (needed in ShowTicketActivity)
                            addTicketTypes(ticketTypes);
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<TicketType>> call, @NonNull Throwable t) {
                            // if ticketTypes could not be retrieved from server, e.g. due to network problems
                            Utils.log(t);
                        }
                    });

        }
    }

    // Event methods

    public void storeEvents(List<Event> events) {
        eventDao.insert(events);
    }

    public void setDismissed(int id) {
        eventDao.setDismissed(id);
    }

    public LiveData<List<Event>> getEvents() {
        return eventDao.getAll();
    }

    /**
     * @return all events for which a ticket exists
     */
    public MediatorLiveData<List<Event>> getBookedEvents() {
        LiveData<List<Ticket>> tickets = ticketDao.getAll();
        MediatorLiveData<List<Event>> events = new MediatorLiveData<>();

        events.addSource(tickets, newTickets -> {
            List<Event> bookedEvents = new ArrayList<>();

            for (Ticket ticket : newTickets) {
                Event event = getEventById(ticket.getEventId());
                // the event may be null if the corresponding event of a ticket has already been deleted
                // these event should not be returned
                if (event != null) {
                    bookedEvents.add(event);
                }
            }

            events.setValue(bookedEvents);
        });

        return events;
    }

    public boolean isEventBooked(Event event) {
        Ticket ticket = ticketDao.getByEventId(event.getId());
        return ticket != null;
    }

    public Event getEventById(int id) {
        return eventDao.getEventById(id);
    }

    // Ticket methods

    public Ticket getTicketByEventId(int eventId) {
        return ticketDao.getByEventId(eventId);
    }

    public TicketType getTicketTypeById(int id) {
        return ticketTypeDao.getById(id);
    }

    public void insert(Ticket... tickets) {
        ticketDao.insert(tickets);
    }

    public void addTicketTypes(List<TicketType> ticketTypes) {
        ticketTypeDao.insert(ticketTypes);
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull @NotNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();

        // Only add the next upcoming event for now
        Event event = eventDao.getNextEvent();
        if (event != null) {
            EventCard eventCard = new EventCard(context);
            eventCard.setEvent(event);
            results.add(eventCard);
        }

        return results;
    }

}

