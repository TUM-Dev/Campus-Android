package de.tum.in.tumcampus.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.LectureDetailsRow;
import de.tum.in.tumcampus.models.LectureDetailsRowSet;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

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
 */
@SuppressLint("DefaultLocale")
public class LecturesDetailsActivity extends ActivityForAccessingTumOnline<LectureDetailsRowSet> implements OnClickListener {

	/** UI elements */
	private Button btnLDetailsTermine;

	/** the current processing Lecture item (model: LectureDetailsRow) */
	private LectureDetailsRow currentitem;
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

	public LecturesDetailsActivity() {
		super(TUMOnlineConst.LECTURES_DETAILS, R.layout.activity_lecturedetails);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == btnLDetailsTermine.getId()) {
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		// Reads lecture id from bundle
		Bundle bundle = this.getIntent().getExtras();
		requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

		super.requestFetch();
	}

	/**
	 * process the given TUMOnline Data and display the details
	 *
	 * @param xmllv Raw text response
	 */
	@Override
	public void onFetch(LectureDetailsRowSet xmllv) {
        // we got exactly one row, that's fine
        currentitem = xmllv.getLehrveranstaltungenDetails().get(0);
        tvLDetailsName.setText(currentitem.getStp_sp_titel().toUpperCase());

        String strLectureLanguage = currentitem.getSemester_name();
        if (currentitem.getHaupt_unterrichtssprache() != null) {
            strLectureLanguage += " - " + currentitem.getHaupt_unterrichtssprache();
        }
        tvLDetailsSemester.setText(strLectureLanguage);
        tvLDetailsSWS.setText(currentitem.getStp_lv_art_name() + " - "
                + currentitem.getDauer_info() + " SWS");
        tvLDetailsDozent.setText(currentitem.getVortragende_mitwirkende());
        tvLDetailsOrg.setText(currentitem.getOrg_name_betreut());
        tvLDetailsInhalt.setText(currentitem.getLehrinhalt());
        tvLDetailsMethode.setText(currentitem.getLehrmethode());
        tvLDetailsZiele.setText(currentitem.getLehrziel());
        tvLDetailsLiteratur.setText(currentitem.getStudienbehelfe());
        tvLDetailsTermin.setText(currentitem.getErsttermin());

        showLoadingEnded();
	}
}
