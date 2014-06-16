package de.tum.in.tumcampus.models;

public class ChatMember {

	private String url = null;
	private String lrz_id;
	private String first_name;
	private String last_name;
	
	public ChatMember(String lrz_id, String first_name, String last_name) {
		super();
		this.lrz_id = lrz_id;
		this.first_name = first_name;
		this.last_name = last_name;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLrzId() {
		return lrz_id;
	}
	public void setLrzId(String lrz_id) {
		this.lrz_id = lrz_id;
	}
	public String getFirstName() {
		return first_name;
	}
	public void setFirstName(String first_name) {
		this.first_name = first_name;
	}
	public String getLastName() {
		return last_name;
	}
	public void setLastName(String last_name) {
		this.last_name = last_name;
	}
}
