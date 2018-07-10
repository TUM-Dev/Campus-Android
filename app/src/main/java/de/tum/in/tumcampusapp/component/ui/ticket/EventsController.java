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
                        if (list == null) list = new ArrayList<Ticket>();
                        ticketDao.insert(list);
                    }

                    @Override
                    public void onFailure(Call<List<Ticket>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            Utils.log(e);
        }

        // Load all ticket types
        try {
            for (Event e : getEvents()) {
                ticketTypeDao.insert(api.getTicketTypes(e.getId()));
            }
        } catch (IOException e) {
            Utils.log(e);
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

    /**
     * This is not a database access but a API call
     * Thus, it needs to be called in a thread
     *
     * @param eventId
     * @return
     */
    public List<TicketType> getTicketTypesByEventId(int eventId) {
        List<TicketType> ticketTypes = null;
        try {
            TUMCabeClient api = TUMCabeClient.getInstance(context);
            ticketTypes = api.getTicketTypes(eventId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ticketTypes;
    }

    public void addTickets(List<Ticket> tickets) {
        ticketDao.insert(tickets);
    }
}

