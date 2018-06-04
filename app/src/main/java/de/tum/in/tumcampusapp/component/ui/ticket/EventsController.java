package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;

/**
 * Mock class, only used to provide static event data for testing purposes
 * TODO: replace this when the actual data is available
 */
public class EventsController {

    /**
     * Only for testing purposes as server calls are not yet implemented
     * -> TODO: replace with real data
     *
     * @return
     */
    public static List<Event> getEvents() {
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

