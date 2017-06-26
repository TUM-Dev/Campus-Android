package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.models.tumo.LectureDetailsRow;
import de.tum.in.tumcampusapp.models.tumo.LectureDetailsRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;


public class GradingDetailsActivity extends ActivityForAccessingTumOnline<LectureDetailsRowSet> {

    public GradingDetailsActivity() {
        super(TUMOnlineConst.LECTURES_DETAILS, R.layout.activity_grading_details);
    }



    private LectureDetailsRow currentItem;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvLDetailsName = (TextView) findViewById(R.id.tvLDetailsName1);
        tvLDetailsSWS = (TextView) findViewById(R.id.tvLDetailsSWS1);
        tvLDetailsSemester = (TextView) findViewById(R.id.tvLDetailsSemester1);
        tvLDetailsDozent = (TextView) findViewById(R.id.tvLDetailsDozent1);
        tvLDetailsOrg = (TextView) findViewById(R.id.tvLDetailsOrg1);
        tvLDetailsInhalt = (TextView) findViewById(R.id.tvLDetailInhalt1);
        tvLDetailsMethode = (TextView) findViewById(R.id.tvLDetailsMethode1);
        tvLDetailsZiele = (TextView) findViewById(R.id.tvLDetailsZiele1);
        tvLDetailsTermin = (TextView) findViewById(R.id.tvLDetailsTermin1);
        tvLDetailsLiteratur = (TextView) findViewById(R.id.tvLDetailsLiteratur1);
        //btnLDetailsTermine = (Button) findViewById(R.id.btnLDetailsTermine);
        //btnLDetailsTermine.setOnClickListener(this);

        // Reads lecture id from bundle
        Bundle bundle = this.getIntent().getExtras();
        requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

        super.requestFetch();
    }




    public void onFetch(LectureDetailsRowSet xmllv) {
        // we got exactly one row, that's fine
        currentItem = xmllv.getLehrveranstaltungenDetails().get(0);
        tvLDetailsName.setText(currentItem.getStp_sp_titel().toUpperCase(Locale.getDefault()));

        StringBuilder strLectureLanguage = new StringBuilder(currentItem.getSemester_name());
        if (currentItem.getHaupt_unterrichtssprache() != null) {
            strLectureLanguage.append(" - ").append(currentItem.getHaupt_unterrichtssprache());
        }
        tvLDetailsSemester.setText(strLectureLanguage);
        tvLDetailsSWS.setText(String.format("%s - %s SWS", currentItem.getStp_lv_art_name(), currentItem.getDauer_info()));
        tvLDetailsDozent.setText(currentItem.getVortragende_mitwirkende());
        tvLDetailsOrg.setText(currentItem.getOrg_name_betreut());
        tvLDetailsInhalt.setText(currentItem.getLehrinhalt());
        tvLDetailsMethode.setText(currentItem.getLehrmethode());
        tvLDetailsZiele.setText(currentItem.getLehrziel());
        tvLDetailsLiteratur.setText(currentItem.getStudienbehelfe());
        tvLDetailsTermin.setText(currentItem.getErsttermin());

        showLoadingEnded();
    }

}
