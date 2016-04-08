package de.tum.in.tumcampusapp.models;

import java.util.Date;

/**
 * Representation of a study room.
 */
public class StudyRoom {

    public final int id;
    public String code = "";
    public String name = "";
    public final Date occupiedTill;

    public StudyRoom(int id, String code, String name, Date occupiedTill) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.occupiedTill = occupiedTill;
    }

    @Override
    public String toString() {
        return "" + code;
    }
}
