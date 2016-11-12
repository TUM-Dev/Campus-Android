package de.tum.in.tumcampusapp.auxiliary.luis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Action {
	
	private ActionType actionType;
	private Map<DataType, String> data = new HashMap<>();
	
	public Action() {
	}
	
	public Action(ActionType actionType) {
		this.actionType = actionType;
	}
	
	public ActionType getActionType() {
		return actionType;
	}
	
	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}
	
	public void addData(DataType dataName, String data) {
		this.data.put(dataName, data);
	}
	
	public String getData(DataType dataName) {
		return this.data.get(dataName);
	}
	
	@Override
	public String toString() {
		StringBuilder actionBuilder = new StringBuilder();
		actionBuilder.append("Action Type: " + actionType + "\n");
		for (Entry<DataType, String> dataEntry : this.data.entrySet()) {
			actionBuilder.append("  " + dataEntry.getKey() + ": " + dataEntry.getValue() + "\n");
		}
		return actionBuilder.toString();
	}
}
