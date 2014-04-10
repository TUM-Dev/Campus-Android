package de.tum.in.tumcampusapp.activities;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import de.tum.in.tumcampus.R;

/**
 * Activity to fetch and display the curricula of different study programs.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 * @review Sascha Moecker
 */
public class CurriculaActivity extends SherlockActivity implements OnItemClickListener {

	public static final String NAME = "name";

	public static final String URL = "url";
	private SharedPreferences sharedPrefs;
	Hashtable<String, String> options;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Counting the number of times that the user used this activity for intelligent reordering
		this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (this.sharedPrefs.getBoolean("implicitly_id", true)) {
			ImplicitCounter.Counter("study_plans_id", this.getApplicationContext());
		}

		this.setContentView(R.layout.activity_curricula);

		ListView list = (ListView) this.findViewById(R.id.activity_curricula_list_view);

		// Puts all hardcoded web addresses into the hash map
		this.options = new Hashtable<String, String>();
		this.options.put(this.getString(R.string.informatics_bachelor_0809),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/informatik/studienplan/studienbeginn-ab-ws-20082009.html");
		this.options.put(this.getString(R.string.informatics_bachelor_1213),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/informatik/studienplan/studienbeginn-ab-ws-20122013.html");
		this.options.put(this.getString(R.string.business_informatics_bachelor_0809),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20082009.html");
		this.options.put(this.getString(R.string.business_informatics_bachelor_1112),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20112012.html");
		this.options.put(this.getString(R.string.business_informatics_bachelor_1213),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20122013.html");
		this.options.put(this.getString(R.string.business_informatics_bachelor_1314),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20132014.html");
		this.options.put(this.getString(R.string.bioinformatics_bachelor),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/bioinformatik/studienplan/ws-20072008.html");
		this.options.put(this.getString(R.string.games_engineering_bachelor),
				"https://www.in.tum.de/fuer-studierende/bachelor-studiengaenge/informatik-games-engineering/studienplan-games.html");
		this.options.put(this.getString(R.string.informatics_master),
				"https://www.in.tum.de/fuer-studierende/master-studiengaenge/informatik/studienplan/fpo-2007-und-fpso-2012.html");
		this.options.put(this.getString(R.string.business_informatics_master),
				"https://www.in.tum.de/fuer-studierende/master-studiengaenge/wirtschaftsinformatik/studienplan/ab-ss-2014.html");

		this.options.put(this.getString(R.string.bioinformatics_master),
				"https://www.in.tum.de/fuer-studierende/master-studiengaenge/bioinformatik/studienplan/ws-20072008.html");
		this.options.put(this.getString(R.string.automotive_master),
				"https://www.in.tum.de/fuer-studierende/master-studiengaenge/automotive-software-engineering/studienplanung.html");
		this.options.put(this.getString(R.string.computational_science_master),
				"https://www.in.tum.de/fuer-studierende/master-studiengaenge/computational-science-and-engineering/curriculum-and-modules.html");

		// Sort curricula options and attach them to the list
		Vector<String> sortedOptions = new Vector<String>(this.options.keySet());
		Collections.sort(sortedOptions);
		String[] optionsArray = sortedOptions.toArray(new String[0]);

		// Sets the adapter with list items
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, optionsArray);
		list = (ListView) this.findViewById(R.id.activity_curricula_list_view);
		list.setAdapter(arrayAdapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		String curriculumName = ((TextView) view).getText().toString();

		// Puts URL and name into an intent and starts the detail view
		Intent intent = new Intent(this, CurriculaDetailsActivity.class);
		intent.putExtra(URL, this.options.get(curriculumName));
		intent.putExtra(NAME, curriculumName);
		this.startActivity(intent);
	}
}
