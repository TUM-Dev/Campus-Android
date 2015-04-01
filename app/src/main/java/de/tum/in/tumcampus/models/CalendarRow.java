package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@SuppressWarnings("UnusedDeclaration")
@Root(name = "event")
public class CalendarRow {
	@Element(required = false)
	private String description = "";

	@Element(required = false)
	private String dtend = "";

	@Element(required = false)
	private String dtstart = "";

	@Element(required = false)
	private Geo geo;

	@Element(required = false)
	private String location = "";

	@Element(required = false)
	private String nr = "";
	// <org_kennung_betreut>TUEIEDA</org_kennung_betreut>

	@Element(required = false)
	private String status = "";
	// <org_name_betreut>Lehrstuhl für Entwurfsautomatisierung (Prof.
	// Schlichtmann)</org_name_betreut>

	@Element(required = false)
	private String title = "";

	@Element(required = false)
	private String url = "";

	// <org_nr_betreut>15393</org_nr_betreut>

	public String getDescription() {
		return description;
	}

	public String getDtend() {
		return dtend;
	}

	public String getDtstart() {
		return dtstart;
	}

	public Geo getGeo() {
		return geo;
	}

	public String getLocation() {
		return location;
	}

	public String getNr() {
		return nr;
	}

	public String getStatus() {
		return status;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

    public void setDtstart(String dtstart) {
        this.dtstart = dtstart;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }
}
