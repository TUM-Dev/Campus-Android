package de.tum.in.tumcampusapp.models.tumcabe;

import java.io.Serializable;

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 */
public class FacilityVote implements Serializable{

    private static final long serialVersionUID=1;

    private int id;
    private Facility facility;
    private boolean vote;
    private String user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public boolean isVote() {
        return vote;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}