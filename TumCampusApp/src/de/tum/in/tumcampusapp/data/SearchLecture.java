package de.tum.in.tumcampusapp.data;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import de.tum.in.tumcampusapp.activities.LecturesDetailsActivity;
import de.tum.in.tumcampusapp.adapters.LecturesSearchListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.LecturesSearchRowSet;

public class SearchLecture extends SearchAction {

	public SearchLecture(Context c) {
		this.context = c;
	}

	@Override
	public BaseAdapter handleResponse(String rawResponse) throws Exception {
		// deserialize the xml output we use simpleXML for this by providing a class which represents the xml-schema
		Serializer serializer = new Persister();
		LecturesSearchRowSet lecturesList = null;

		lecturesList = serializer.read(LecturesSearchRowSet.class, rawResponse);

		// no results found
		if (lecturesList == null) {
			return null;
		}

		Log.d(this.getClass().getSimpleName(), lecturesList.getLehrveranstaltungen().size() + "");

		// make some customizations to the ListView provide data via the FindLecturesListAdapter
		BaseAdapter ret = new LecturesSearchListAdapter(this.context, lecturesList.getLehrveranstaltungen());
		return ret;
	}

	@Override
	public String getTumAction() {
		return Const.LECTURES_SEARCH;
	}

	@Override
	public Class getDetailsActivity() {
		return LecturesDetailsActivity.class;
	}

	@Override
	public Bundle getBundle(Object o) {
		LecturesSearchRow item = (LecturesSearchRow) o;

		// bundle data for the LectureDetails Activity
		Bundle bundle = new Bundle();
		bundle.putString(item.STP_SP_NR, item.getStp_sp_nr());
		return bundle;
	}

}
