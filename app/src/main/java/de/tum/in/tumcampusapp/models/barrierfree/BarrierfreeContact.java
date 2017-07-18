package de.tum.in.tumcampusapp.models.barrierfree;

import de.tum.in.tumcampusapp.adapters.SimpleStickyListHeadersAdapter;

/**
 * The model used to display contact infromation in barrier free page
 */
public class BarrierfreeContact implements SimpleStickyListHeadersAdapter.SimpleStickyListItem{
//    private int id;
    private String name;
    private String telephone;
    private String email;
    private String faculty;
    private String tumID = "";

    public BarrierfreeContact(String name, String phone, String email, String faculty, String tumonlineID){
        this.name = name;
        this.telephone = phone;
        this.email = email;
        this.faculty = faculty;
        this.tumID = tumonlineID;
    }

    public BarrierfreeContact(){

    }

    public String getName(){
        return name;
    }

    public String getPhone(){
        return telephone;
    }

    public String getEmail(){
        return email;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getTumonlineID() {
        return tumID;
    }

    @Override
    public String getHeadName() {
        return getFaculty();
    }

    @Override
    public String toString() {
        return "BarrierfreeContact{" +
                "name='" + name + '\'' +
                ", phone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", faculty='" + faculty + '\'' +
                ", tumonlineID='" + tumID + '\'' +
                '}';
    }
}
