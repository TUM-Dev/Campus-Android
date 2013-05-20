package de.tum.in.tumcampusapp.models;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Group that describes some organizational entity a person/employee can belong to. Note: This model is based on the
 * TUMOnline web service response format for a corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
@Root(name = "gruppe", strict = false)
public class Group implements Serializable {

	private static final long serialVersionUID = -3430225489964912473L;

	@Element(name = "beschreibung", required = false)
	private String description;

	@Element(name = "kennung")
	private String id;

	@Element(name = "org")
	private String org;

	@Element(name = "titel", required = false)
	private String title;

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getOrg() {
		return org;
	}

	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}