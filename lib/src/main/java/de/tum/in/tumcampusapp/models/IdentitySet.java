package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "rowset")
public class IdentitySet {

	@ElementList(inline = true)
	private List<Identity> ids;

	@Override
	public String toString() {
		if(ids==null || ids.size()==0)
		     return null;
		return ids.get(0).toString();
	}
}
