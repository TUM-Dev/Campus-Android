package de.tum.in.tumcampusapp.component.ui.ticket;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

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
    public static List<Ticket> getTickets() {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add( new Ticket(EventsController.getEventById(2), "7585685764567467657",
                new TicketType(45, 4.5, "good tickets"), 0, false));
        tickets.add( new Ticket(EventsController.getEventById(3), "ljipu3rupo567467657",
                new TicketType(23, 2.5, "good tickets"),0, false));
        return tickets;
    }

    public static Ticket getTicketByEventId(int eventId) {
        for (Ticket ticket : getTickets()){
            if(ticket.getEvent().getId() == eventId){
                return ticket;
            }
        }
        return null;
    }
}