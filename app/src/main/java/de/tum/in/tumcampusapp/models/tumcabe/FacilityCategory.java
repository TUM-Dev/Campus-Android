package de.tum.in.tumcampusapp.models.tumcabe;

import java.io.Serializable;

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 */
public class FacilityCategory implements Serializable {

    private static final long serialVersionUID=1;

    private int id;
    private String name;

    public FacilityCategory(Integer id) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}