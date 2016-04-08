package de.tum.in.tumcampusapp.models;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Representation of a study room group
 */
public class StudyRoomGroup implements Comparable<StudyRoomGroup> {

    public final int id;
    public String name = "";
    public String details = "";
    public final List<StudyRoom> rooms;

    public StudyRoomGroup(int id, String name, String details, List<StudyRoom> rooms) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NonNull StudyRoomGroup studyRoomGroup) {
        return name.compareTo(studyRoomGroup.name);
    }
}
