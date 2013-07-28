package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "geo")
public class Geo {
	@Element(required = false)
	private String latitude="0";
	
	public String getLatitude() {
		return latitude;
	}

	@Element(required = false)
	private String longitude="0";

	public String getLongitude() {
		return longitude;
	}
	
}