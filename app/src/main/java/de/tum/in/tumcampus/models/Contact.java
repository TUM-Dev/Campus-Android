package de.tum.in.tumcampus.models;

import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * Contact information of a TUM {@link Employee} or a generic {@link Person}.
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
public class Contact implements Serializable {

	private static final long serialVersionUID = 4413581972047241018L;
	@Element(name = "zusatz_info", required = false)
	private String addInfo;

	@Element(name = "fax", required = false)
	private String fax;

	@Element(name = "www_homepage", required = false)
	private String homepage;

	@Element(name = "mobiltelefon", required = false)
	private String mobilephone;

	// TODO Think of exporting to string.xml or constants
	@Element(name = "telefon", required = false)
	private String telefon;

	public String getAdditionalInfo() {
		return addInfo;
	}

	public String getFax() {
		return fax;
	}

	public String getHomepage() {
		return homepage;
	}

	public String getMobilephone() {
		return mobilephone;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setAdditionalInfo(String addInfo) {
		this.addInfo = addInfo;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}
}
