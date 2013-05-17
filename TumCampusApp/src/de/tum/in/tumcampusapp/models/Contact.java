package de.tum.in.tumcampusapp.models;

import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Contact information of a TUM {@link Employee} or a generic {@link Person}. Note: This model is based on the TUMOnline
 * web service response format for a corresponding request.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class Contact implements Serializable {

	private static final long serialVersionUID = 4413581972047241018L;
	// TODO Think of exporting to string.xml or constants
	@Element(name = "telefon", required = false)
	private String telefon;

	@Element(name = "fax", required = false)
	private String fax;

	@Element(name = "mobiltelefon", required = false)
	private String mobilephone;

	@Element(name = "zusatz_info", required = false)
	private String addInfo;

	@Element(name = "www_homepage", required = false)
	private String homepage;

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getMobilephone() {
		return mobilephone;
	}

	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}

	public String getAdditionalInfo() {
		return addInfo;
	}

	public void setAdditionalInfo(String addInfo) {
		this.addInfo = addInfo;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
}
