package de.tum.in.tumcampus.models;

public class ChatMember {

	private String url;
	private String lrzId;
	private String firstName;
	private String lastName;
	
	public ChatMember(String url, String lrzId, String firstName, String lastName) {
		super();
		this.url = url;
		this.lrzId = lrzId;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLrzId() {
		return lrzId;
	}
	public void setLrzId(String lrzId) {
		this.lrzId = lrzId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
