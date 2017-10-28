package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.OrgDetailItemList;
import de.tum.in.tumcampusapp.models.tumo.OrgDetailsItem;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;

/**
 * Show all details that are available on TUMCampus to any organisation
 */
public class OrganisationDetailsActivity extends ActivityForAccessingTumOnline<OrgDetailItemList> {

    /**
     * Id of the organisation of which the details should be shown
     */
    private String orgId;

    /**
     * Only for setting it in the caption at the top
     */
    private String orgName;

    public OrganisationDetailsActivity() {
        super(TUMOnlineConst.Companion.getORG_DETAILS(), R.layout.activity_organisationdetails);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the submitted (bundle) data
        Bundle bundle = this.getIntent()
                            .getExtras();
        orgId = bundle.getString(Const.ORG_ID);
        orgName = bundle.getString(Const.ORG_NAME);
    }

    @Override
    public void onStart() {
        super.onStart();
        // if there is a call of OrganisationDetails without an id (should not
        // be possible)
        if (orgId == null) {
            Utils.showToast(this, R.string.invalid_organisation);
            return;
        }

        // set the name of the organisation as heading (TextView tvCaption)
        // only load the details if the details page is new and it isn't a
        // return from a link
        TextView tvCaption = findViewById(R.id.tvCaption);
        if (tvCaption.getText()
                     .toString()
                     .compareTo(orgName) != 0) {

            // set the new organisation name in the heading
            tvCaption.setText(orgName.toUpperCase(Locale.getDefault()));

            // Initialise the request handler and append the orgUnitID to the URL
            requestHandler.setParameter("pOrgNr", orgId);
            super.requestFetch();
        }
    }

    /**
     * Initialize BackButton -> On Click: Go to Organisation.java and show the
     * Organisation Tree
     *
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public void onBackPressed() {

        // if button "back" is clicked -> make a new Bundle with the orgId and
        // start Organisation-Activity
        Intent intent = new Intent(this, OrganisationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Bundle bundle = new Bundle();
        bundle.putString(Const.ORG_ID, orgId);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    /**
     * When the data has arrived call this function, parse the Data and Update
     * the UserInterface
     *
     * @param result XML-TUMCampus-Response (String)
     */
    @Override
    public void onFetch(OrgDetailItemList result) {
        updateUI(result.getGroups()
                       .get(0));
        showLoadingEnded();
    }

    /**
     * Show the Organisation Details to the user
     *
     * @param organisation organisation detail object
     */
    private void updateUI(OrgDetailsItem organisation) {
        // catch error
        if (organisation == null) {
            return;
        }

        TextView identifier = findViewById(R.id.identifier);
        TextView name = findViewById(R.id.name);
        TextView contact = findViewById(R.id.contact);
        TextView address = findViewById(R.id.adress);
        TextView homepage = findViewById(R.id.homepage);
        TextView email = findViewById(R.id.email);
        TextView phone = findViewById(R.id.phone);
        TextView fax = findViewById(R.id.fax);
        TextView secretary = findViewById(R.id.secretary);
        TextView extraCaption = findViewById(R.id.extra_name);
        TextView extra = findViewById(R.id.extra);
        TextView bib = findViewById(R.id.bib);

        identifier.setText(organisation.getCode());
        name.setText(organisation.getName());
        contact.setText(organisation.getContactName());
        address.setText(organisation.getContactStreet());
        homepage.setText(organisation.getContactLocationURL());
        String mail = organisation.getContactEmail();
        mail = mail.replace("ä", "ae");
        mail = mail.replace("ö", "oe");
        mail = mail.replace("ü", "ue");
        email.setText(mail);
        phone.setText(organisation.getContactTelephone());
        fax.setText(organisation.getContactFax());
        secretary.setText(organisation.getContactLocality());
        extraCaption.setText(organisation.getAdditionalInfoCaption());
        extra.setText(organisation.getAdditionalInfoText());
        bib.setText(organisation.getContactLocality());

        if (identifier.getText()
                      .length() == 0) {
            ((View) identifier.getParent()).setVisibility(View.GONE);
        }
        if (name.getText()
                .length() == 0) {
            ((View) name.getParent()).setVisibility(View.GONE);
        }
        if (contact.getText()
                   .length() == 0) {
            ((View) contact.getParent()).setVisibility(View.GONE);
        }
        if (address.getText()
                   .length() == 0) {
            ((View) address.getParent()).setVisibility(View.GONE);
        }
        if (homepage.getText()
                    .length() == 0) {
            ((View) homepage.getParent()).setVisibility(View.GONE);
        }
        if (email.getText()
                 .length() == 0) {
            ((View) email.getParent()).setVisibility(View.GONE);
        }
        if (phone.getText()
                 .length() == 0) {
            ((View) phone.getParent()).setVisibility(View.GONE);
        }
        if (fax.getText()
               .length() == 0) {
            ((View) fax.getParent()).setVisibility(View.GONE);
        }
        if (secretary.getText()
                     .length() == 0) {
            ((View) secretary.getParent()).setVisibility(View.GONE);
        }
        if (extraCaption.getText()
                        .length() == 0) {
            ((View) extraCaption.getParent()).setVisibility(View.GONE);
        }
        if (extra.getText()
                 .length() == 0) {
            ((View) extra.getParent()).setVisibility(View.GONE);
        }
        if (bib.getText()
               .length() == 0) {
            ((View) bib.getParent()).setVisibility(View.GONE);
        }
    }
}