package de.tum.in.tumcampusapp.models;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * An employee of the TUM.
 * <p>
 * Note: This model is based on the TUMOnline web service response format for a corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
@Root(name = "person", strict = false)
public class Employee extends Person implements Serializable {

	private static final long serialVersionUID = -6276330922677632119L;

	@Element(name = "titel", required = false)
	private String title;

	@Element(required = false)
	private String email;

	@Element(name = "sprechstunde", required = false)
	private String consultationHours;

	@Element(name = "dienstlich")
	private Contact businessContact;

	@Element(name = "privat")
	private Contact privateContact;

	@Element(name = "image_data", required = false)
	private String imageData;

	@Element(name = "raeume", required = false)
	private RoomList rooms;

	@Element(name = "gruppen", required = false)
	private GroupList groups;

	@Element(name = "telefon_nebenstellen", required = false)
	private TelSubstationList telSubstations;

	public String getTitle() {
		return title;
	}

	public String getEmail() {
		return email;
	}

	public String getConsultationHours() {
		return consultationHours;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setConsultationHours(String consultationHours) {
		this.consultationHours = consultationHours;
	}

	public String getImageData() {
		return imageData;
	}

	public void setImageData(String base64String) {
		this.imageData = base64String;
	}

	public Bitmap getImage() {
		byte[] imageAsBytes = Base64.decode(imageData.getBytes(), Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
	}

	public Contact getBusinessContact() {
		return businessContact;
	}

	public void setBusinessContact(Contact businessContact) {
		this.businessContact = businessContact;
	}

	public Contact getPrivateContact() {
		return privateContact;
	}

	public void setPrivateContact(Contact privateContact) {
		this.privateContact = privateContact;
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

	public void setRoomList(RoomList rooms) {
		this.rooms = rooms;
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

	public void setTelSubstationList(TelSubstationList telSubstations) {
		this.telSubstations = telSubstations;
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

	public void setGroups(GroupList groups) {
		this.groups = groups;
	}
}
