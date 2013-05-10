package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.LectureDetailsRow;
import de.tum.in.tumcampusapp.models.LectureDetailsRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * This Activity will show all details found on the TUMOnline web service
 * identified by its lecture id (which has to be posted to this activity by
 * bundle).
 * 
 * There is also the opportunity to get all appointments which are related to
 * this lecture by clicking the button on top of the view.
 * 
 * HINT: a valid TUM Online token is needed
 * 
 * NEEDS: stp_sp_nr set in incoming bundle (lecture id)
 * 
 * needed/linked files: res.layout.lecture_details, LectureAppointments
 * 
 * @solves [M6] Details einer Lehrveranstaltung ausgeben
 * @author Daniel G. Mayr
 * @review Thomas Behrens // i found nothing tbd.
 */
public class LecturesDetailsActivity extends Activity implements OnClickListener, TUMOnlineRequestFetchListener {

	private static final String VERANSTALTUNGEN_DETAILS = "veranstaltungenDetails";

	/** UI elements */
	private Button btnLDetailsTermine;
	/** the current processing Lecture item (model: LectureDetailsRow) */
	private LectureDetailsRow currentitem;
	/** Handler to send request to TUMOnline */
	private TUMOnlineRequest requestHandler;
	private TextView tvLDetailsDozent;
	private TextView tvLDetailsInhalt;
	private TextView tvLDetailsLiteratur;
	private TextView tvLDetailsMethode;
	private TextView tvLDetailsName;
	private TextView tvLDetailsOrg;
	private TextView tvLDetailsSemester;
	private TextView tvLDetailsSWS;

	private TextView tvLDetailsTermin;

	private TextView tvLDetailsZiele;

	@Override
	public void onClick(View v) {
		if (v.getId() == btnLDetailsTermine.getId()) {
			// start LectureAppointments
			Bundle bundle = new Bundle();
			// LectureAppointments need the name and id of the facing lecture
			bundle.putString("stp_sp_nr", currentitem.getStp_sp_nr());
			bundle.putString(Const.TITLE_EXTRA, currentitem.getStp_sp_titel());

			Intent i = new Intent(this, LecturesAppointmentsActivity.class);
			i.putExtras(bundle);
			startActivity(i);
		}
	}

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lecturedetails);

		// bind UI elements
		tvLDetailsName = (TextView) findViewById(R.id.tvLDetailsName);
		tvLDetailsSWS = (TextView) findViewById(R.id.tvLDetailsSWS);
		tvLDetailsSemester = (TextView) findViewById(R.id.tvLDetailsSemester);
		tvLDetailsDozent = (TextView) findViewById(R.id.tvLDetailsDozent);
		tvLDetailsOrg = (TextView) findViewById(R.id.tvLDetailsOrg);
		tvLDetailsInhalt = (TextView) findViewById(R.id.tvLDetailInhalt);
		tvLDetailsMethode = (TextView) findViewById(R.id.tvLDetailsMethode);
		tvLDetailsZiele = (TextView) findViewById(R.id.tvLDetailsZiele);
		tvLDetailsTermin = (TextView) findViewById(R.id.tvLDetailsTermin);
		tvLDetailsLiteratur = (TextView) findViewById(R.id.tvLDetailsLiteratur);

		btnLDetailsTermine = (Button) findViewById(R.id.btnLDetailsTermine);
		btnLDetailsTermine.setOnClickListener(this);
		// Linkify.addLinks(tvLDetailsDozent, Pattern.compile("00.00.000"),
		// "tel:");
	}

	/**
	 * process the given TUMOnline Data and display the details
	 * 
	 * @param rawResponse
	 */
	@Override
	public void onFetch(String rawResponse) {

		// deserialize
		Serializer serializer = new Persister();
		try {
			LectureDetailsRowSet xmllv = serializer.read(LectureDetailsRowSet.class, rawResponse);
			// only take first one (there should only be one)
			if (xmllv.getLehrveranstaltungenDetails().size() != 1) {
				Toast.makeText(this,
						getString(R.string.something_wrong) + ": " + xmllv.getLehrveranstaltungenDetails().size() + " " + getString(R.string.elements_found),
						10000).show();
			} else {
				// we got exactly one row, thats fine
				currentitem = xmllv.getLehrveranstaltungenDetails().get(0);
				tvLDetailsName.setText(currentitem.getStp_sp_titel());

				String strLectureLanguage = currentitem.getSemester_name();
				if (currentitem.getHaupt_unterrichtssprache() != null) {
					strLectureLanguage += " - " + currentitem.getHaupt_unterrichtssprache();
				}
				tvLDetailsSemester.setText(strLectureLanguage);
				tvLDetailsSWS.setText(currentitem.getStp_lv_art_name() + " - " + currentitem.getDauer_info() + " SWS");
				tvLDetailsDozent.setText(currentitem.getVortragende_mitwirkende());
				tvLDetailsOrg.setText(currentitem.getOrg_name_betreut());
				tvLDetailsInhalt.setText(currentitem.getLehrinhalt());
				tvLDetailsMethode.setText(currentitem.getLehrmethode());
				tvLDetailsZiele.setText(currentitem.getLehrziel());
				tvLDetailsLiteratur.setText(currentitem.getStudienbehelfe());
				tvLDetailsTermin.setText(currentitem.getErsttermin());
			}
		} catch (Exception e) {
			// well, something went obviously wrong
			Log.d("conv", "wont work: " + e.toString());
			e.printStackTrace();
			Toast.makeText(this, "An error occured: " + e.getMessage(), 20000).show();
		}
	}

	@Override
	public void onFetchCancelled() {
		// ignore
	}

	/**
	 * while fetching a TUMOnline Request an error occurred this will show the
	 * error message in a toast
	 */
	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(this, errorReason);
	}

	@Override
	public void onStart() {
		super.onStart();

		// the usual one: prepare the request to TUMOnline web service
		requestHandler = new TUMOnlineRequest(VERANSTALTUNGEN_DETAILS, this);

		// read lecture id from bundle
		Bundle bundle = this.getIntent().getExtras();
		requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

		requestHandler.fetchInteractive(this, this);
	}

}
