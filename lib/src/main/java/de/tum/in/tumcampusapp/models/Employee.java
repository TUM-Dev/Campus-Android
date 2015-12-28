package de.tum.in.tumcampusapp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * An employee of the TUM.
 * <p/>
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "person", strict = false)
public class Employee extends Person implements Serializable {

    private static final long serialVersionUID = -6276330922677632119L;

    @Element(name = "dienstlich")
    private Contact businessContact;

    @Element(name = "sprechstunde", required = false)
    private String consultationHours = "";

    @Element(required = false)
    private String email = "";

    @Element(name = "gruppen", required = false)
    private GroupList groups;

    @Element(name = "image_data", required = false)
    private String imageData = "";

    @Element(name = "privat")
    private Contact privateContact;

    @Element(name = "raeume", required = false)
    private RoomList rooms;

    @Element(name = "telefon_nebenstellen", required = false)
    private TelSubstationList telSubstations;

    @Element(name = "titel", required = false)
    private String title = "";

    public Contact getBusinessContact() {
        return businessContact;
    }

    public String getConsultationHours() {
        return consultationHours;
    }

    public String getEmail() {
        return email;
    }

    public GroupList getGroupList() {
        return groups;
    }

    public List<Group> getGroups() {
        if (groups != null) {
            return groups.getGroups();
        }
        return null;
    }

    public Bitmap getImage() {
        byte[] imageAsBytes = Base64.decode(imageData.getBytes(),
                Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0,
                imageAsBytes.length);
    }

    public String getImageData() {
        return imageData;
    }

    public Contact getPrivateContact() {
        return privateContact;
    }

    public RoomList getRoomList() {
        return rooms;
    }

    public List<Room> getRooms() {
        if (rooms != null) {
            return rooms.getRooms();
        }
        return null;
    }

    public TelSubstationList getTelSubstationList() {
        return telSubstations;
    }

    public List<TelSubstation> getTelSubstations() {
        if (telSubstations != null) {
            return telSubstations.getSubstations();
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        String infoText = "";

        String title = getTitle();
        if (title != null) {
            infoText = title + " ";
        }

        return infoText + getName() + " " + getSurname();
    }
}
