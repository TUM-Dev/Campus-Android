package de.tum.in.tumcampusapp.models.barrierfree;

/**
 * The model used to display contact infromation in barrier free page
 */
public class BarrierfreeContact {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String faculty;
    private String tumonlineID = "";

    public BarrierfreeContact(int id, String name, String phone, String email, String faculty){
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.faculty = faculty;
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getPhone(){
        return phone;
    }

    public String getEmail(){
        return email;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setTumonlineID(String tumonlineID) {
        this.tumonlineID = tumonlineID;
    }

    public String getTumonlineID() {
        return tumonlineID;
    }
}
