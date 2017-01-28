package de.tum.in.tumcampusapp.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Generated;

@Entity
public class Faculty {

    @Id(assignable = true)
    private long faculty;
    private String name;

    @Generated(hash = 494660752)
    public Faculty(long faculty, String name) {
        this.faculty = faculty;
        this.name = name;
    }
    @Generated(hash = 2112390923)
    public Faculty() {
    }

    public long getFaculty() {
        return faculty;
    }
    public void setFaculty(long faculty) {
        this.faculty = faculty;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}