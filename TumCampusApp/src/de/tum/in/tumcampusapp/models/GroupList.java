package de.tum.in.tumcampusapp.models;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Wrapper class holding a list of groups of an employee. Note: This model is based on the TUMOnline web service
 * response format for a corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
@Root(name = "gruppen")
public class GroupList implements Serializable {

	private static final long serialVersionUID = 2713473533623487005L;

	@ElementList(inline = true, required = false)
	private List<Group> groups;

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

}
