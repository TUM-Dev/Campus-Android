package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Wrapper class holding a list of persons. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 */

@Root(name = "rowset")
public class PersonList {

    @ElementList(inline = true, required = false)
    private List<Person> persons;

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

}
