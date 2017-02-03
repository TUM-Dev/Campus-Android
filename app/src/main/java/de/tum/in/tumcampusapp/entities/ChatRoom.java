package de.tum.in.tumcampusapp.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Generated;

@Entity
public class ChatRoom {
    @Id
    private Long id;

    private Integer room = -1;

    private String name;
    private String semester;
    private String semesterId;
    private Integer joined = -1;
    private Integer _id = 0;
    private String contributor;
    private int members = 0;

    @Generated(hash = 21543601)
    public ChatRoom(Long id, Integer room, String name, String semester,
                    String semesterId, Integer joined, Integer _id, String contributor,
                    int members) {
        this.id = id;
        this.room = room;
        this.name = name;
        this.semester = semester;
        this.semesterId = semesterId;
        this.joined = joined;
        this._id = _id;
        this.contributor = contributor;
        this.members = members;
    }

    @Generated(hash = 507512638)
    public ChatRoom() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoom() {
        return room;
    }

    public void setRoom(Integer room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public Integer getJoined() {
        return joined;
    }

    public void setJoined(Integer joined) {
        this.joined = joined;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

}