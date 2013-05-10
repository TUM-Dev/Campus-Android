package de.tum.in.tumcampusapp.activities;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;

/**
 * Activity to fetch and display the curricula of different programs.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class CurriculaActivity extends Activity implements OnItemClickListener {

	public static final String NAME = "name";

	public static final String URL = "url";
	Hashtable<String, String> options;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_curricula);

		ListView list = (ListView) findViewById(R.id.activity_curricula_list_view);

		options = new Hashtable<String, String>();
		options.put(getString(R.string.informatics_bachelor),
				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/informatik/studienplan/studienbeginn-ab-ws-20072008.html");
		options.put(getString(R.string.business_informatics_bachelor_0809),
				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20112012.html");
		options.put(getString(R.string.business_informatics_bachelor_1112),
				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20082009.html");
		options.put(getString(R.string.bioinformatics_bachelor),
				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/bioinformatik/studienplan/ws-20072008.html");
		options.put(getString(R.string.games_engineering_bachelor),
				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/informatik-games-engineering/studienplan-games.html");
		options.put(getString(R.string.informatics_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/informatik/studienplan/studienplan-fpo-2007.html");
		options.put(getString(R.string.business_informatics_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/wirtschaftsinformatik/studienplan");

		options.put(getString(R.string.bioinformatics_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/bioinformatik/studienplan/ws-20072008.html");
		options.put(getString(R.string.automotive_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/automotive-software-engineering/studienplanung.html");
		options.put(getString(R.string.computational_science_master),
				"http://www.in.tum.de/fuer-studieninteressierte/master-studiengaenge/computational-science-and-engineering/course/course-plan.html");

		// sort curricula options and attach them to the list
		Vector<String> sortedOptions = new Vector<String>(options.keySet());
		Collections.sort(sortedOptions);
		String[] optionsArray = sortedOptions.toArray(new String[0]);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, optionsArray);
		list = (ListView) findViewById(R.id.activity_curricula_list_view);
		list.setAdapter(arrayAdapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		String curriculumName = ((TextView) view).getText().toString();

		Intent intent = new Intent(this, CurriculaActivityDetails.class);
		intent.putExtra(URL, options.get(curriculumName));
		intent.putExtra(NAME, curriculumName);
		startActivity(intent);
	}
}
