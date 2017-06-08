package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.Utils;

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

	private boolean is_first_on_day = false;

	// <org_nr_betreut>15393</org_nr_betreut>

	public String getDescription() {
		return description;
	}

	public String getDtend() {
		return dtend;
	}

	public Date getDtendDate(){
		return Utils.getISODateTime(this.dtend);
	}

	public String getDtstart() {
		return dtstart;
	}

	public Date getDtstartDate(){
		return Utils.getISODateTime(this.dtstart);
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

	public boolean isFirstOnDay() {
		return this.is_first_on_day;
	}

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDtend(String dtend) {
        this.dtend = dtend;
    }

    public void setDtstart(String dtstart) {
        this.dtstart = dtstart;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void setIsFirstOnDay(Boolean is_first_on_day){
		this.is_first_on_day = is_first_on_day;
	}

    @Override
    public String toString() {
        return "CalendarRow{" +
                "description='" + description + '\'' +
                ", dtend='" + dtend + '\'' +
                ", dtstart='" + dtstart + '\'' +
                ", geo=" + geo +
                ", location='" + location + '\'' +
                ", nr='" + nr + '\'' +
                ", status='" + status + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
