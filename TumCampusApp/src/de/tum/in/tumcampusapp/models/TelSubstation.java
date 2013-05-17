package de.tum.in.tumcampusapp.models;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Telefon substation to reach an employee. Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 */
@Root(name = "nebenstelle", strict = false)
public class TelSubstation implements Serializable {

	private static final long serialVersionUID = -3289209179488515142L;

	@Element(name = "telefonnummer")
	private String number;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
