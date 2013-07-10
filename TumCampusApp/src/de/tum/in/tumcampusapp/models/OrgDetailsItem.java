package de.tum.in.tumcampusapp.models;

import de.tum.in.tumcampusapp.adapters.OrgDetailsItemHandler;

/**
 * Modell for the Organisation Details collected by
 * {@link OrgDetailsItemHandler}
 * 
 * @author Thomas Behrens
 */

public class OrgDetailsItem {

	/**
	 * Information about the organisation
	 */
	// Organisation Identification Number
	private String id;
	// Organisation Name
	private String name;
	// Organisation Code, e.g. "TUZESSB"
	private String code;
	// Organisation Description, eg. "Weitere Angebote und Vorträge..."
	private String description;

	/**
	 * Information about how to get in contact with organisation
	 */
	// Contact Name of Organisation, similar to Organisation name
	private String contactName;
	// Street and Street number of the Organisation
	private String contactStreet;
	// Town, to which the organisation belongs to
	private String contactLocality;
	// PLZ, e.g. "80333"
	private String contactPLZ;
	// Country, to which the organisation belongs to
	private String contactCountry;
	// Phone Number, usually to the secretary of the organisation
	private String contactTelephone;
	// Type of phone number, e.g. "office"
	private String contactTelephoneType;
	// Fax Number
	private String contactFax;
	// Email Address
	private String contactEmail;
	// Website of Organisation
	private String contactLink;
	// URL to a Googlemaps Site containing the
	private String contactLocationURL;
	// TUMOnline link
	private String contactAdditionalInfo;
	// TUMCampus link
	private String tumCampusLink;

	/**
	 * Additional Info Text: Can be very different filled
	 */
	// Caption
	private String additionalInfoCaption;
	// Information
	private String additionalInfoText;

	public OrgDetailsItem() {
		super();
		this.id = "";
		this.name = "";
		this.code = "";
		this.description = "";
		this.contactName = "";
		this.contactStreet = "";
		this.contactLocality = "";
		this.contactPLZ = "";
		this.contactCountry = "";
		this.contactTelephone = "";
		this.contactFax = "";
		this.contactEmail = "";
		this.contactLink = "";
		this.contactLocationURL = "";
		this.contactAdditionalInfo = "";
		this.tumCampusLink = "";
		this.contactTelephoneType = "";
		this.additionalInfoCaption = "";
		this.additionalInfoText = "";
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the contactName
	 */
	public String getContactName() {
		return contactName;
	}

	/**
	 * @param contactName
	 *            the contactName to set
	 */
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	/**
	 * @return the contactStreet
	 */
	public String getContactStreet() {
		return contactStreet;
	}

	/**
	 * @param contactStreet
	 *            the contactStreet to set
	 */
	public void setContactStreet(String contactStreet) {
		this.contactStreet = contactStreet;
	}

	/**
	 * @return the contactLocality
	 */
	public String getContactLocality() {
		return contactLocality;
	}

	/**
	 * @param contactLocality
	 *            the contactLocality to set
	 */
	public void setContactLocality(String contactLocality) {
		this.contactLocality = contactLocality;
	}

	/**
	 * @return the contactPLZ
	 */
	public String getContactPLZ() {
		return contactPLZ;
	}

	/**
	 * @param contactPLZ
	 *            the contactPLZ to set
	 */
	public void setContactPLZ(String contactPLZ) {
		this.contactPLZ = contactPLZ;
	}

	/**
	 * @return the contactCountry
	 */
	public String getContactCountry() {
		return contactCountry;
	}

	/**
	 * @param contactCountry
	 *            the contactCountry to set
	 */
	public void setContactCountry(String contactCountry) {
		this.contactCountry = contactCountry;
	}

	/**
	 * @return the contactTelephone
	 */
	public String getContactTelephone() {
		return contactTelephone;
	}

	/**
	 * @param contactTelephone
	 *            the contactTelephone to set
	 */
	public void setContactTelephone(String contactTelephone) {
		this.contactTelephone = contactTelephone;
	}

	/**
	 * @return the contactFax
	 */
	public String getContactFax() {
		return contactFax;
	}

	/**
	 * @param contactFax
	 *            the contactFax to set
	 */
	public void setContactFax(String contactFax) {
		this.contactFax = contactFax;
	}

	/**
	 * @return the contactEmail
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * @param contactEmail
	 *            the contactEmail to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * @return the contactLink
	 */
	public String getContactLink() {
		return contactLink;
	}

	/**
	 * @param contactLink
	 *            the contactLink to set
	 */
	public void setContactLink(String contactLink) {
		this.contactLink = contactLink;
	}

	/**
	 * @return the contactLocationURL
	 */
	public String getContactLocationURL() {
		return contactLocationURL;
	}

	/**
	 * @param contactLocationURL
	 *            the contactLocationURL to set
	 */
	public void setContactLocationURL(String contactLocationURL) {
		this.contactLocationURL = contactLocationURL;
	}

	/**
	 * @return the contactAdditionalInfo
	 */
	public String getContactAdditionalInfo() {
		return contactAdditionalInfo;
	}

	/**
	 * @param contactAdditionalInfo
	 *            the contactAdditionalInfo to set
	 */
	public void setContactAdditionalInfo(String contactAdditionalInfo) {
		this.contactAdditionalInfo = contactAdditionalInfo;
	}

	/**
	 * @return the tumCampusLink
	 */
	public String getTumCampusLink() {
		return tumCampusLink;
	}

	/**
	 * @param tumCampusLink
	 *            the tumCampusLink to set
	 */
	public void setTumCampusLink(String tumCampusLink) {
		this.tumCampusLink = tumCampusLink;
	}

	/**
	 * @return the contactTelephoneType
	 */
	public String getContactTelephoneType() {
		return contactTelephoneType;
	}

	/**
	 * @param contactTelephoneType
	 *            the contactTelephoneType to set
	 */
	public void setContactTelephoneType(String contactTelephoneType) {
		this.contactTelephoneType = contactTelephoneType;
	}

	/**
	 * @return the additionalInfoCaption
	 */
	public String getAdditionalInfoCaption() {
		return additionalInfoCaption;
	}

	/**
	 * @param additionalInfoCaption
	 *            the additionalInfoCaption to set
	 */
	public void setAdditionalInfoCaption(String additionalInfoCaption) {
		this.additionalInfoCaption = additionalInfoCaption;
	}

	/**
	 * @return the additionalInfoText
	 */
	public String getAdditionalInfoText() {
		return additionalInfoText;
	}

	/**
	 * @param additionalInfoText
	 *            the additionalInfoText to set
	 */
	public void setAdditionalInfoText(String additionalInfoText) {
		this.additionalInfoText = additionalInfoText;
	}
}
