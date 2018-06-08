package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.GregorianCalendar;

import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;

/**
 * Mock class, only used to provide static ticket data for testing purposes
 * TODO: replace this when the actual data is available
 */
public class TicketsController {

    /**
     * Only for testing purposes as server calls are not yet implemented
     * -> TODO: replace with real data
     *
     * @return
     */
    public static Ticket getTickets() {
        Ticket ticket = new Ticket(new Event(0, "http://placehold.it/120x120&text=image1", "Unity",
                "Keine Ahnung war noch nie dort. Soll ganz cool sein...",
                "Garching, Magistrale",
                new GregorianCalendar(2018, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"), "7585685764567467657", new TicketType(45, 4.5, "good tickets"));
        return ticket;
    }
}