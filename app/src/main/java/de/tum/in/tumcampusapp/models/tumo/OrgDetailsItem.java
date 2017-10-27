package de.tum.in.tumcampusapp.models.tumo;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Model for the organisation details
 */
@SuppressWarnings("FieldCanBeLocal")
@Root(name = "row")
public class OrgDetailsItem {
    /**
     * Information about the organisation
     */
    // Organisation Identification Number
    @Element(name = "nr")
    private String id = "";

    // Organisation Name
    @Element(name = "name")
    private String name = "";

    /**
     * Additional Info Text: Can be very different filled
     */
    // Caption
    @Element(name = "zusatz_info_name", required = false)
    private String additionalInfoCaption = "";
    // Information
    @Element(name = "zusatz_info", required = false)
    private String additionalInfoText = "";
    // Organisation Code, e.g. "TUZESSB"
    @Element(name = "kennung")
    private String code = "";

    // Email Address
    @Element(name = "email_adresse", required = false)
    private String contactEmail = "";
    // Fax Number
    @Element(name = "fax_nummer", required = false)
    private String contactFax = "";
    // Town, to which the organisation belongs to
    @Element(name = "sekretariat_info", required = false)
    private String contactLocality = "";
    // URL to a Google Maps Site containing the
    @Element(name = "www_homepage", required = false)
    private String contactLocationURL = "";

    /**
     * Information about how to get in contact with organisation
     */
    // Contact Name of Organisation, similar to Organisation name
    @Element(name = "ansprechpartner", required = false)
    private String contactName = "";
    // Street and Street number of the Organisation
    @Element(name = "adresse_text", required = false)
    private String contactStreet = "";
    // Phone Number, usually to the secretary of the organisation
    @Element(name = "telefon_nummer", required = false)
    private String contactTelephone = "";

    @Element(name = "bibliothek_info", required = false)
    private String contactAdditionalInfo = "";

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
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
