package de.tum.in.tumcampusapp.models;

/**
 * Presents the faculty model that is used in fetching the facultyData from server
 */
public class Faculty {
    private String faculty;
    private String name;

    public Faculty(String id, String name) {
        this.faculty = id;
        this.name = name;
    }

    public String getId() {
        return faculty;
    }

    public void setId(String id) {
        this.faculty = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}