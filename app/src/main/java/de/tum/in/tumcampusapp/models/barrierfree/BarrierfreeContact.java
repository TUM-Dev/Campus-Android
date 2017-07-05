package de.tum.in.tumcampusapp.models.barrierfree;

/**
 * The model used to display contact infromation in barrier free page
 */
public class BarrierfreeContact {
//    private int id;
    private String name;
    private String phone;
    private String email;
    private String faculty;
    private String tumonlineID = "";

    public BarrierfreeContact(String name, String phone, String email, String faculty){
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.faculty = faculty;
    }

    public BarrierfreeContact(){

    }

//    public int getId() {
//        return id;
//    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public void setTumonlineID(String tumonlineID) {
        this.tumonlineID = tumonlineID;
    }

    public String getTumonlineID() {
        return tumonlineID;
    }

    @Override
    public String toString() {
        return "BarrierfreeContact{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", faculty='" + faculty + '\'' +
                ", tumonlineID='" + tumonlineID + '\'' +
                '}';
    }
}
