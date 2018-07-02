package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.news.NewsDao;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;
import de.tum.in.tumcampusapp.utils.sync.SyncManager;

import static de.tum.in.tumcampusapp.utils.CacheManager.VALIDITY_ONE_DAY;

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

            // NOTE: the dummy data on the server contains a dummy ticket for event id 2
            //       thus, we add another event with id 2 for testing purposes
            // TODO: remove this when dummy data on server is made consistent or real data is used!
            /*Event event = events.get(0);

            events.add(new Event(2,event.getImage(), event.getTitle() + "2",
                    event.getDescription() + " Not to say extremely nice!",
                    event.getLocality(), event.getDate(), event.getLink()));
            */
            eventDao.insert(events);
        } catch (IOException e) {
            Utils.log(e);
        }

        // Load all tickets
        try {
            // TODO: replace by real user id (dummy ticket with user id 1 for now)
            List tickets = api.getTickets(1);
            ticketDao.insert(tickets);
        } catch (IOException e) {
            Utils.log(e);
        }

        // Load all ticket types
        try {
            // TODO: replace by real event ids! -> loop over all found ids
            List ticketTypes = api.getTicketTypes(1);
            ticketTypeDao.insert(ticketTypes);
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
        for (Ticket ticket : tickets){
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

