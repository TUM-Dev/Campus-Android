package de.tum.in.tumcampusapp.data;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import de.tum.in.tumcampusapp.activities.PersonsDetailsActivity;
import de.tum.in.tumcampusapp.adapters.PersonListAdapter;
import de.tum.in.tumcampusapp.models.Person;
import de.tum.in.tumcampusapp.models.PersonList;

public class SearchPerson extends SearchAction {

	public SearchPerson(Context c) {
		this.context = c;
	}

	@Override
	public BaseAdapter handleResponse(String rawResponse) throws Exception {
		// test by sample element "familienname" (required field)
		if (!rawResponse.contains("familienname")) {
			return null;
		}

		// deserialize the xml output we use simpleXML for this by providing a class which represents the xml-schema
		Serializer serializer = new Persister();
		PersonList personList = null;

		personList = serializer.read(PersonList.class, rawResponse);

		// no results found
		if (personList == null) {
			return null;
		}

		Log.d(this.getClass().getSimpleName(), personList.getPersons().size() + "");

		// make some customizations to the ListView provide data via the FindLecturesListAdapter
		BaseAdapter ret = new PersonListAdapter(this.context, personList.getPersons());
		return ret;
	}

	@Override
	public String getTumAction() {
		return "personenSuche";
	}

	@Override
	public Class getDetailsActivity() {
		return PersonsDetailsActivity.class;
	}

	@Override
	public Bundle getBundle(Object o) {
		Person person = (Person) o;

		// bundle data for the LectureDetails Activity
		Bundle bundle = new Bundle();
		bundle.putSerializable("personObject", person);
		return bundle;
	}

}
