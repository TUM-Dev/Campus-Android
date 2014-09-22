package de.tum.in.tumcampus.models;

import de.tum.in.tumcampus.adapters.OrgDetailsItemHandler;

/**
 * Modell for the Organisation Details collected by
 * {@link OrgDetailsItemHandler}
 */

public class OrgDetailsItem {

	/**
	 * Additional Info Text: Can be very different filled
	 */
	// Caption
	private String additionalInfoCaption;
	// Information
	private String additionalInfoText;
	// Organisation Code, e.g. "TUZESSB"
	private String code;
	// TUMOnline link
	private String contactAdditionalInfo;

	// Country, to which the organisation belongs to
	private String contactCountry;
	// Email Address
	private String contactEmail;
	// Fax Number
	private String contactFax;
	// Website of Organisation
	private String contactLink;
	// Town, to which the organisation belongs to
	private String contactLocality;
	// URL to a Googlemaps Site containing the
	private String contactLocationURL;
	/**
	 * Information about how to get in contact with organisation
	 */
	// Contact Name of Organisation, similar to Organisation name
	private String contactName;
	// PLZ, e.g. "80333"
	private String contactPLZ;
	// Street and Street number of the Organisation
	private String contactStreet;
	// Phone Number, usually to the secretary of the organisation
	private String contactTelephone;
	// Type of phone number, e.g. "office"
	private String contactTelephoneType;
	// Organisation Description, eg. "Weitere Angebote und Vorträge..."
	private String description;
	/**
	 * Information about the organisation
	 */
	// Organisation Identification Number
	private String id;

	// Organisation Name
	private String name;
	// TUMCampus link
	private String tumCampusLink;

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
	 * @return the additionalInfoCaption
	 */
	public String getAdditionalInfoCaption() {
		return additionalInfoCaption;
	}

	/**
	 * @return the additionalInfoText
	 */
	public String getAdditionalInfoText() {
		return additionalInfoText;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the contactAdditionalInfo
	 */
	public String getContactAdditionalInfo() {
		return contactAdditionalInfo;
	}

	/**
	 * @return the contactCountry
	 */
	public String getContactCountry() {
		return contactCountry;
	}

	/**
	 * @return the contactEmail
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * @return the contactFax
	 */
	public String getContactFax() {
		return contactFax;
	}

	/**
	 * @return the contactLink
	 */
	public String getContactLink() {
		return contactLink;
	}

	/**
	 * @return the contactLocality
	 */
	public String getContactLocality() {
		return contactLocality;
	}

	/**
	 * @return the contactLocationURL
	 */
	public String getContactLocationURL() {
		return contactLocationURL;
	}

	/**
	 * @return the contactName
	 */
	public String getContactName() {
		return contactName;
	}

	/**
	 * @return the contactPLZ
	 */
	public String getContactPLZ() {
		return contactPLZ;
	}

	/**
	 * @return the contactStreet
	 */
	public String getContactStreet() {
		return contactStreet;
	}

	/**
	 * @return the contactTelephone
	 */
	public String getContactTelephone() {
		return contactTelephone;
	}

	/**
	 * @return the contactTelephoneType
	 */
	public String getContactTelephoneType() {
		return contactTelephoneType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the tumCampusLink
	 */
	public String getTumCampusLink() {
		return tumCampusLink;
	}

	/**
	 * @param additionalInfoCaption
	 *            the additionalInfoCaption to set
	 */
	public void setAdditionalInfoCaption(String additionalInfoCaption) {
		this.additionalInfoCaption = additionalInfoCaption;
	}

	/**
	 * @param additionalInfoText
	 *            the additionalInfoText to set
	 */
	public void setAdditionalInfoText(String additionalInfoText) {
		this.additionalInfoText = additionalInfoText;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @param contactAdditionalInfo
	 *            the contactAdditionalInfo to set
	 */
	public void setContactAdditionalInfo(String contactAdditionalInfo) {
		this.contactAdditionalInfo = contactAdditionalInfo;
	}

	/**
	 * @param contactCountry
	 *            the contactCountry to set
	 */
	public void setContactCountry(String contactCountry) {
		this.contactCountry = contactCountry;
	}

	/**
	 * @param contactEmail
	 *            the contactEmail to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * @param contactFax
	 *            the contactFax to set
	 */
	public void setContactFax(String contactFax) {
		this.contactFax = contactFax;
	}

	/**
	 * @param contactLink
	 *            the contactLink to set
	 */
	public void setContactLink(String contactLink) {
		this.contactLink = contactLink;
	}

	/**
	 * @param contactLocality
	 *            the contactLocality to set
	 */
	public void setContactLocality(String contactLocality) {
		this.contactLocality = contactLocality;
	}

	/**
	 * @param contactLocationURL
	 *            the contactLocationURL to set
	 */
	public void setContactLocationURL(String contactLocationURL) {
		this.contactLocationURL = contactLocationURL;
	}

	/**
	 * @param contactName
	 *            the contactName to set
	 */
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	/**
	 * @param contactPLZ
	 *            the contactPLZ to set
	 */
	public void setContactPLZ(String contactPLZ) {
		this.contactPLZ = contactPLZ;
	}

	/**
	 * @param contactStreet
	 *            the contactStreet to set
	 */
	public void setContactStreet(String contactStreet) {
		this.contactStreet = contactStreet;
	}

	/**
	 * @param contactTelephone
	 *            the contactTelephone to set
	 */
	public void setContactTelephone(String contactTelephone) {
		this.contactTelephone = contactTelephone;
	}

	/**
	 * @param contactTelephoneType
	 *            the contactTelephoneType to set
	 */
	public void setContactTelephoneType(String contactTelephoneType) {
		this.contactTelephoneType = contactTelephoneType;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param tumCampusLink
	 *            the tumCampusLink to set
	 */
	public void setTumCampusLink(String tumCampusLink) {
		this.tumCampusLink = tumCampusLink;
	}
}
