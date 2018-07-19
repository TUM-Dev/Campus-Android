package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
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

public class EventsController implements ProvidesCard{

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
        eventDao = TcaDb.getInstance(context).eventDao();
        ticketDao = TcaDb.getInstance(context).ticketDao();
        ticketTypeDao = TcaDb.getInstance(context).ticketTypeDao();
    }

    public void downloadFromService() {
        Callback<List<Event>> eventCallback = new Callback<List<Event>>() {

            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                List<Event> events = response.body();
                if (events == null) {
                    events = new ArrayList<>();
                }
                addEvents(events);
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Utils.log(t);
            }
        };

        Callback<List<Ticket>> ticketCallback = new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                List<Ticket> tickets = response.body();
                if (tickets == null) {
                    tickets = new ArrayList<>();
                }
                addTickets(tickets);
                loadTicketTypesForTickets(tickets);
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                Utils.log(t);
            }
        };

        getEventsAndTicketsFromServer(eventCallback, ticketCallback);
    }

    public void getEventsAndTicketsFromServer(Callback<List<Event>> eventCallback,
                                              Callback<List<Ticket>> ticketCallback){
        // Delete all too old items
        eventDao.cleanUp();

        // Load all events
        TUMCabeClient.getInstance(context).getEvents(eventCallback);

        // Load all tickets
        try {
            if (Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class) != null) {
                TUMCabeClient.getInstance(context).getTickets(context, ticketCallback);
            }
        } catch (IOException e) {
            Utils.log(e);
        }
    }

    private void loadTicketTypesForTickets(List<Ticket> tickets){
        // get ticket type information for all tickets
        for (Ticket ticket : tickets){
            TUMCabeClient.getInstance(context).getTicketTypes(ticket.getEventId(),
                    new Callback<List<TicketType>>(){

                        @Override
                        public void onResponse(Call<List<TicketType>> call, Response<List<TicketType>> response) {
                            List<TicketType> ticketTypes = response.body();
                            if (ticketTypes == null) {
                                ticketTypes = new ArrayList<>();
                            }
                            // add found ticket types to database (needed in ShowTicketActivity)
                            addTicketTypes(ticketTypes);
                        }

                        @Override
                        public void onFailure(Call<List<TicketType>> call, Throwable t) {
                            // if ticketTypes could not be retrieved from server, e.g. due to network problems
                            Utils.log(t);
                        }
                    });

        }
    }

    // Event methods

    public void addEvents(List<Event> events) {
        eventDao.insert(events);
    }

    public List<Event> getEvents() {
        return eventDao.getAll();
    }

    public List<Event> getBookedEvents() {
        // Return all events for which a ticket exists
        List<Ticket> tickets = ticketDao.getAll();
        List<Event> bookedEvents = new ArrayList<>();
        for (Ticket ticket : tickets) {
            bookedEvents.add(getEventById(ticket.getEventId()));
        }
        return bookedEvents;
    }

    public boolean isEventBooked(Event event) {
        for (Event bookedEvent : getBookedEvents()) {
            if (bookedEvent.getId() == event.getId()) {
                return true;
            }
        }
        return false;
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

    public void addTickets(List<Ticket> tickets) {
        ticketDao.insert(tickets);
    }

    public void addTicketTypes(List<TicketType> ticketTypes) {
        ticketTypeDao.insert(ticketTypes);
    }

    @NotNull
    @Override
    public List<Card> getCards() {
        List<Card> results = new ArrayList<>();

        // Only add the next upcoming event for now
        Event event = eventDao.getNextEvent();
        if (event != null){
            EventCard eventCard = new EventCard(context);
            eventCard.setEvent(event);
            results.add(eventCard);
        }

        return results;
    }
}

