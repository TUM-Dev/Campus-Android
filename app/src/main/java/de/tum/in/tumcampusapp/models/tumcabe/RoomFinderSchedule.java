package de.tum.in.tumcampusapp.models.tumcabe;

public class RoomFinderSchedule {
    String start;
    String end;
    long event_id;
    String title;

    public RoomFinderSchedule(String start, String end, long event_id, String title) {
        this.start = start;
        this.end = end;
        this.event_id = event_id;
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public long getEvent_id() {
        return event_id;
    }

    public String getTitle() {
        return title;
    }
}
