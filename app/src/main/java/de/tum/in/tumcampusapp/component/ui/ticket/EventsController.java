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
                "Die TUM Campus App wird von Freiwilligen und Studenten entwickelt. " +
                        "Die App eignet sich sowohl zur Verwendung auf Smartphones, wie auch Tablets und bietet " +
                        "unter anderem folgende Funktionen:Vorlesungstermine, Neuigkeiten von TUM relevanten " +
                        "Organisationen, Veranstaltungshinweise, Mensa Speiseplan, MVV Abfahrtszeiten, " +
                        "Umgebungspläne und viele weitere Funktionen. ",
                "Garching, Magistrale",
                new GregorianCalendar(2018, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        events.add(new Event(2, "http://placehold.it/120x120&text=image2", "Unity 2",
                "Die TUM Campus App wird von Freiwilligen und Studenten entwickelt. " +
                        "Die App eignet sich sowohl zur Verwendung auf Smartphones, wie auch Tablets und bietet " +
                        "unter anderem folgende Funktionen:Vorlesungstermine, Neuigkeiten von TUM relevanten " +
                        "Organisationen, Veranstaltungshinweise, Mensa Speiseplan, MVV Abfahrtszeiten, " +
                        "Umgebungspläne und viele weitere Funktionen. ",
                "Garching, Magistrale",
                new GregorianCalendar(2019, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        events.add(new Event(3, "http://placehold.it/120x120&text=image2", "Unity 3",
                "Die TUM Campus App wird von Freiwilligen und Studenten entwickelt. " +
                        "Die App eignet sich sowohl zur Verwendung auf Smartphones, wie auch Tablets und bietet " +
                        "unter anderem folgende Funktionen:Vorlesungstermine, Neuigkeiten von TUM relevanten " +
                        "Organisationen, Veranstaltungshinweise, Mensa Speiseplan, MVV Abfahrtszeiten, " +
                        "Umgebungspläne und viele weitere Funktionen. ",
                "Garching, Magistrale",
                new GregorianCalendar(2020, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        events.add(new Event(4, "http://placehold.it/120x120&text=image2", "Unity 4",
                "Die TUM Campus App wird von Freiwilligen und Studenten entwickelt. " +
                        "Die App eignet sich sowohl zur Verwendung auf Smartphones, wie auch Tablets und bietet " +
                        "unter anderem folgende Funktionen:Vorlesungstermine, Neuigkeiten von TUM relevanten " +
                        "Organisationen, Veranstaltungshinweise, Mensa Speiseplan, MVV Abfahrtszeiten, " +
                        "Umgebungspläne und viele weitere Funktionen. ",
                "Garching, Magistrale",
                new GregorianCalendar(2021, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        return events;
    }
    public static List<Event> getbookedEvents() {
        List<Event> events = new ArrayList<>();
        events.add(new Event(0, "http://placehold.it/120x120&text=image1", "Unity2",
                "Die TUM Campus App wird von Freiwilligen und Studenten entwickelt. " +
                        "Die App eignet sich sowohl zur Verwendung auf Smartphones, wie auch Tablets und bietet " +
                        "unter anderem folgende Funktionen:Vorlesungstermine, Neuigkeiten von TUM relevanten " +
                        "Organisationen, Veranstaltungshinweise, Mensa Speiseplan, MVV Abfahrtszeiten, " +
                        "Umgebungspläne und viele weitere Funktionen. ",
                "Garching, Magistrale",
                new GregorianCalendar(2018, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        events.add(new Event(2, "http://placehold.it/120x120&text=image2", "Unity 3",
                "Kurze Beschreibung.",
                "Garching, Magistrale",
                new GregorianCalendar(2019, 8, 8).getTime(),
                "https://mpi.fs.tum.de/fuer-studierende/veranstaltungen/unity/"));
        return events;
    }
}

