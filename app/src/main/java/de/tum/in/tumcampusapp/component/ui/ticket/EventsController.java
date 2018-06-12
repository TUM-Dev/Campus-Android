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
        events.add(new Event(1, "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/30704066_10156075719240336_971954058317266944_o.jpg?_nc_cat=0&oh=870eb884fbe5f493e049c70b59fc70d1&oe=5BA83F31", "TUNIX 2018",
                "Auch im Sommer 2018 stellt sich für Münchner Studierende erneut die Frage: Warum bei schönem Wetter im Hörsaal sitzen, wenn man auch draußen im TUNIX-Biergarten sein könnte? Dann ist es bereits das 38. Mal, dass Studierende der Technischen Universität München Bühne und Biergarten auf der Wiese zwischen Glyptothek und Mensa errichten.",
                "Arcisstraße 17, 80333",
                new GregorianCalendar(2018, 6, 5).getTime(),
                "https://www.facebook.com/events/369289900243997/"));

        events.add(new Event(2, "https://mpi.fs.tum.de/wordpress/wp-content/uploads/2016/10/banner_ball.jpg", "WINTERBALL",
                "Wir freuen uns dieses Jahr schon zum dritten Mal den Winterball zu veranstalten. Am 24.11. öffnen wir im MI-Gebäude wieder die Pforten für einen winterlichen Tanzabend. Für den kleinen Hunger zwischendurch ist mit Häppchen gesorgt. Getränke können an der Bar gekauft werden.",
                "Boltzmannstr. 3, 85748",
                new GregorianCalendar(2018, 11, 24).getTime(),
                "https://www.facebook.com/events/315001705559152/"));
        events.add(new Event(3, "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/32308876_1719567341432183_1385171384396677120_o.jpg?_nc_cat=0&oh=6fc99c924ce44dc60dbd19caa9eccccb&oe=5BC4215C", "GARNIX 2018",
                "Es ist so weit: Das Garchinger Kulturfestival mit Biergarten-Charme - kurz gesagt das #GARNIX - steht vor der Tür, heuer allerdings nicht vor der Fakultät für Chemie, sondern auf dem Vorplatz des Mathe/Info-Gebäudes!",
                "Vorplatz Mathe Informatik, Boltzmannstraße 3, 85748",
                new GregorianCalendar(2018, 6, 18).getTime(),
                "https://www.facebook.com/events/1655640211224074/"));
        events.add(new Event(4, "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/34561792_1949757885037095_8460877383170260992_n.jpg?_nc_cat=0&oh=05320ed40f56232b1f406e86baed4fc5&oe=5BAF5FC0", "TUM SOM Midterm Party",
                "Na, schon angefangen zu lernen? Halt Stopp!! Es ist wieder soweit: Die Fachschaft TUM SOM feiert mit Dir ihre legendäre Midterm Party! Am Donnerstag, den 21.06. geht’s in der 089 Bar rund. Feier zu extra nicem House und Hits die ganze Nacht ab! Die Klausuren können warten!",
                "Maximiliansplatz 5, 80333",
                new GregorianCalendar(2018, 6, 21).getTime(),
                "https://www.facebook.com/events/2048930862027728/"));
        return events;
    }

    public static List<Event> getBookedEvents() {
        List<Event> events = new ArrayList<>();
        events.add(new Event(2, "https://mpi.fs.tum.de/wordpress/wp-content/uploads/2016/10/banner_ball.jpg", "WINTERBALL",
                "Wir freuen uns dieses Jahr schon zum dritten Mal den Winterball zu veranstalten. Am 24.11. öffnen wir im MI-Gebäude wieder die Pforten für einen winterlichen Tanzabend. Für den kleinen Hunger zwischendurch ist mit Häppchen gesorgt. Getränke können an der Bar gekauft werden.",
                "Boltzmannstr. 3, 85748",
                new GregorianCalendar(2018, 11, 24).getTime(),
                "https://www.facebook.com/events/315001705559152/"));
        events.add(new Event(3, "https://scontent-frx5-1.xx.fbcdn.net/v/t1.0-9/32308876_1719567341432183_1385171384396677120_o.jpg?_nc_cat=0&oh=6fc99c924ce44dc60dbd19caa9eccccb&oe=5BC4215C", "GARNIX 2018",
                "Es ist so weit: Das Garchinger Kulturfestival mit Biergarten-Charme - kurz gesagt das #GARNIX - steht vor der Tür, heuer allerdings nicht vor der Fakultät für Chemie, sondern auf dem Vorplatz des Mathe/Info-Gebäudes!",
                "Vorplatz Mathe Informatik, Boltzmannstraße 3, 85748",
                new GregorianCalendar(2018, 6, 18).getTime(),
                "https://www.facebook.com/events/1655640211224074/"));
        return events;
    }

    public static boolean isEventBooked(Event event) {
        for (Event bookedEvent : getBookedEvents()) {
            if (bookedEvent.getId() == event.getId()) {
                return true;
            }
        }
        return false;
    }

    public static Event getEventById(int id) {
        for (Event event : getEvents()) {
            if (event.getId() == id) {
                return event;
            }
        }
        return null;
    }
}

