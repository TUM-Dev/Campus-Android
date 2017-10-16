package de.tum.in.tumcampusapp.models.tumcabe;

import com.google.common.base.Optional;

import java.io.Serializable;


/**
 * Presents the faculty model that is used in fetching the facultyData from server
 */
public class Facility implements Serializable{

    private static final long serialVersionUID=1;

    private int id;
    private String name;
    private Double longitude;
    private Double latitude;
    private FacilityCategory facilityCategory;
    private int member;
    private int downVotes;
    private int upVotes;
    private Integer myVote;
    private String map;

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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public FacilityCategory getFacilityCategory() {
        return facilityCategory;
    }

    public void setFacilityCategory(FacilityCategory facilityCategory) {
        this.facilityCategory = facilityCategory;
    }

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }

    public int getDownVotes() {
        return downVotes;
    }

    public void setDownVotes(int downVotes) {
        this.downVotes = downVotes;
    }

    public int getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(int upVotes) {
        this.upVotes = upVotes;
    }

    public Integer getMyVote() {
        return myVote;
    }

    public void setMyVote(Integer myVote) {
        this.myVote = myVote;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }
}