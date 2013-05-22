package de.tum.in.tumcampusapp.models;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Wrapper class holding a list of persons. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrnes
 */

@Root(name = "rowset")
public class PersonList {

	@ElementList(inline = true)
	private List<Person> persons;

	public List<Person> getPersons() {
		return persons;
	}

	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}

}
