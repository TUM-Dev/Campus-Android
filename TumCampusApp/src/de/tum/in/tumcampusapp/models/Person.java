package de.tum.in.tumcampusapp.models;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * A person, often an {@link Employee} working at TUM.
 * <p>
 * Note: This model is based on the TUMOnline web service response format for a corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
@Root(name = "row", strict = false)
public class Person implements Serializable {

	private static final long serialVersionUID = -5210814076506102292L;

	public static final String MALE = "M";
	public static final String FEMALE = "W";

	@Element(name = "obfuscated_id")
	private String id;

	@Element(name = "vorname")
	private String name;

	@Element(name = "familienname")
	private String surname;

	@Element(name = "geschlecht", required = false)
	private String gender;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		if (surname == null) {
			return "";
		}
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
}
