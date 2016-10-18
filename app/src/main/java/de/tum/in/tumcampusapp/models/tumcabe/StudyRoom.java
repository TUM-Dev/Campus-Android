package de.tum.in.tumcampusapp.models.tumcabe;

import java.util.Date;

/**
 * Representation of a study room.
 */
public class StudyRoom {

    public final int id;
    public String code = "";
    public String name = "";
    public String location = "";
    public final Date occupiedTill;

    public StudyRoom(int id, String code, String name, String location, Date occupiedTill) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.location = location;
        this.occupiedTill = occupiedTill;
    }

    @Override
    public String toString() {
        return "" + code;
    }
}
