package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "event")
public class KalendarRow {
	@Element(required = false)
	private String nr;
	// <org_kennung_betreut>TUEIEDA</org_kennung_betreut>

	@Element(required = false)
	private String status;
	// <org_name_betreut>Lehrstuhl für Entwurfsautomatisierung (Prof.
	// Schlichtmann)</org_name_betreut>

	@Element(required = false)
	private String url;
	// <org_nr_betreut>15393</org_nr_betreut>
	
	@Element(required = false)
	private String title;
	
	@Element(required = false)
	private String description;
	
	@Element(required = false)
	private String dtstart;
	
	@Element(required = false)
	private String dtend;
	
	@Element(required = false)
	private String location;
	
	@Element(required = false)
	private Geo geo;
	
	public Geo getGeo() {
		return geo;
	}

	public String getNr() {
		return nr;
	}
	
	public String getStatus() {
		return status;
	}
	public String getUrl() {
		return url;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public String getDtstart() {
		return dtstart;
	}
	public String getDtend() {
		return dtend;
	}
	public String getLocation() {
		return location;
	}
}
