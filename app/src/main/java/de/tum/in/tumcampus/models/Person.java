package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * A person, often an {@link Employee} working at TUM.
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "row", strict = false)
public class Person implements Serializable {

	public static final String FEMALE = "W";

	public static final String MALE = "M";
	private static final long serialVersionUID = -5210814076506102292L;

	@Element(name = "geschlecht", required = false)
	private String gender;

	@Element(name = "obfuscated_id")
	private String id;

	@Element(name = "vorname")
	private String name;

	@Element(name = "familienname")
	private String surname;

	public String getGender() {
		return gender;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	public String getSurname() {
		if (surname == null) {
			return "";
		}
		return surname;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return getName() + " " + getSurname();
    }
}
