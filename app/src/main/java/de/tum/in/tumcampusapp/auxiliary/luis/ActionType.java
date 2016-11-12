package de.tum.in.tumcampusapp.auxiliary.luis;

public enum ActionType {
	MENSA_MENU,
	MENSA_LOCATION,
	MENSA_TIME,
	
	TRANSPORTATION_LOCATION,
	TRANSPORTATION_TIME,
	
	PROFESSOR_INFORMATION,
	
	// Errors
	ERROR_BAD_INPUT,
	ERROR_ENTITIES,
	ERROR;
	
	private String entityInput;
	
	public void setEntityInput(String entityInput) {
		this.entityInput = entityInput;
	}
	
	public String getEntityInput() {
		return this.entityInput;
	}
}
