package de.tum.in.tumcampusapp.data;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;

public abstract class SearchAction {

	protected Context context;

	public abstract String getTumAction();

	public abstract Class getDetailsActivity();

	public abstract BaseAdapter handleResponse(String rawResponse) throws Exception;

	public abstract Bundle getBundle(Object o);

}
