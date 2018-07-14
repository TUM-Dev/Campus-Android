package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsController {

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


    public void downloadFromService(boolean force) {
        TUMCabeClient api = TUMCabeClient.getInstance(context);

        // Delete all too old items
        eventDao.cleanUp();

        // Load all events since the last sync
        try {
            List<Event> events = api.getEvents();
            eventDao.insert(events);
        } catch (IOException e) {
            Utils.log(e);
        }

        // Load all tickets
        try {
            if (Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class) != null) {
                api.getTickets(context, new Callback<List<Ticket>>() {
                    @Override
                    public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                        List<Ticket> list = response.body();
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        ticketDao.insert(list);
                        loadTicketTypesForTickets(list);
                    }

                    @Override
                    public void onFailure(Call<List<Ticket>> call, Throwable t) {
                        Utils.log(t);
                    }
                });
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
}

