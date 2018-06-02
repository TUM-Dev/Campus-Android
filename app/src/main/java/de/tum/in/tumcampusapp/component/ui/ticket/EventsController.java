package de.tum.in.tumcampusapp.component.ui.ticket;

import android.content.Context;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.utils.CacheManager.VALIDITY_ONE_DAY;

public class EventsController implements ProvidesCard {

    private static final int TIME_TO_SYNC = VALIDITY_ONE_DAY;
    private final Context context;

    // TODO: replace by database connection (@Dao classes etc.; see NewDao etc. for reference)
    private SparseArray<Event> eventsMap = new SparseArray<>();

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public EventsController(Context context) {
        this.context = context;
    }

    /**
     * Download events from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     */
    public void downloadFromExternal(boolean force) {
        TUMCabeClient api = TUMCabeClient.getInstance(context);

        // Load all events
        try {
            List<Event> events = api.getEvents();

            // Add events to map
            if (events != null){
                for (Event event : events){
                    eventsMap.put(event.getId(), event);
                }
            }
        } catch (IOException e) {
            Utils.log(e);
        }

        // TODO: remove this (only to test server calls)
        try {
            List<Ticket> tickets = api.getTickets();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestCard(@NotNull Context context) {
        // TODO
    }

    /**
     * Only for testing purposes as server calls are not yet implemented
     * -> TODO: replace with real data
     * @return
     */
    public static List<Event> getEvents(){
        List<Event> events = new ArrayList<>();
        events.add(new Event(0, "http://placehold.it/120x120&text=image1", "Unity",
                "Keine Ahnung war noch nie dort. Soll ganz cool sein...",
                "Garching, Magistrale",
                new GregorianCalendar(2018, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        events.add(new Event(1, "http://placehold.it/120x120&text=image2", "Unity 2",
                "Das gleiche nochmal",
                "Garching, Magistrale",
                new GregorianCalendar(2019, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        return events;
    }
}
