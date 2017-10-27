package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.OrgItemListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.tumo.OrgItem;
import de.tum.in.tumcampusapp.models.tumo.OrgItemList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;

/**
 * Activity that shows the first level of organisations at TUM.
 */
public class OrganisationActivity extends ActivityForAccessingTumOnline<OrgItemList> implements OnClickListener {
    /**
     * To show at start the highest Organisation level (The highest
     * Organisations are child of "Organisation 1" = TUM)
     */
    private static final String TOP_LEVEL_ORG = "1";

    private static boolean languageGerman;

    /**
     * List of Organisations shown on the Display
     */
    private ListView lvOrg;

    /**
     * orgId is the ID of the organisation you click on
     */
    private String orgId = TOP_LEVEL_ORG;

    /**
     * orgName is the name of the parent organisation, whose folder is showed
     */
    private String orgName;

    /**
     * parentId is the ID of the parent organisation, of which the
     * sub-organisations are showed
     */
    private String parentId = TOP_LEVEL_ORG;

    private OrgItemList result;

    public OrganisationActivity() {
        super(TUMOnlineConst.ORG_TREE, R.layout.activity_organisation);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // list of organizations
        lvOrg = findViewById(R.id.lstOrganisations);

        // set language = German if system language is German else set English
        languageGerman = System.getProperty("user.language")
                               .compareTo(Const.DE) == 0;

        // get all organisations information
        requestFetch();
    }

    @Override
    public void onFetch(OrgItemList rawResponse) {
        result = rawResponse;
        if (languageGerman) {
            orgName = getParent(parentId).getNameDe();
        } else {
            orgName = getParent(parentId).getNameEn();
        }
        showItems(parentId);
        showLoadingEnded();
    }

    /**
     * Returns true if there are one or more elements in the organisation tree
     * inside this organisation
     *
     * @param organisationId organisation id
     * @return True if it exists, false otherwise
     */
    private boolean existSubOrganisation(String organisationId) {
        for (OrgItem item : result.getGroups()) {
            if (item.getParentId()
                    .equals(organisationId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches for the parentId of an element, if it is already in the highest layer, it returns 1.
     *
     * @param parentId parent id
     * @return organisation item
     */
    OrgItem getParent(String parentId) {
        OrgItem parentObject = new OrgItem();
        for (OrgItem item : result.getGroups()) {
            // if there is an organisation that has the given parentId as organisationId
            // make a parent element and return it
            if (item.getId()
                    .equals(parentId)) {
                parentObject.setId(item.getParentId());
                parentObject.setNameDe(languageGerman ? item.getNameDe() : item.getNameEn());
                return parentObject;
            }
        }

        // if no parent found => jump to start layer
        parentObject.setId(TOP_LEVEL_ORG);
        return parentObject;
    }

    /**
     * A click on the BackButton should show the parent class or go back to the main menu
     */
    @Override
    public void onBackPressed() {
        // go back to the main menu, if the user is in the highest level
        if (orgId.equals(TOP_LEVEL_ORG)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return;
        }

        // get one layer up
        orgId = parentId;
        OrgItem p = getParent(orgId);
        parentId = p.getId();

        // Switch language
        if (languageGerman) {
            orgName = getParent(parentId).getNameDe();
        } else {
            orgName = getParent(parentId).getNameEn();
        }
        showItems(orgId);
    }

    /**
     * Show all items in a certain layer having a parent element with parent_id
     * parent.
     *
     * @param orgItem all items with the same parent
     */
    void showItems(String orgItem) {

        // caption button gets caption
        TextView tvCaption = findViewById(R.id.tvCaption);

        // if no orgName -> highest level
        if (orgName == null) {
            orgName = getString(R.string.tum);
        }

        // set caption (organisation "folder" name)
        tvCaption.setText(orgName.toUpperCase(Locale.getDefault()));

        List<OrgItem> organisationList = new ArrayList<>();

        // go through the XML file and give each organisation its Id, German
        // name, English name and parent-Id
        for (OrgItem item : result.getGroups()) {
            if (item.getParentId()
                    .equals(orgItem)) {
                organisationList.add(item);
            }
        }

        lvOrg.setAdapter(new OrgItemListAdapter(this, organisationList));

        // action for clicks on a list-item
        lvOrg.setOnItemClickListener((a, v, position, id) -> {
            Object o = lvOrg.getItemAtPosition(position);
            OrgItem org = (OrgItem) o;

            // look if no subOrganisation exists, and if not make bundle and
            // start OrganisationDetails
            if (existSubOrganisation(org.getId())) {
                // if subOrganisation exists, show subOrganisation structure
                parentId = orgId;
                orgId = org.getId();
                // switch correct language
                if (languageGerman) {
                    orgName = org.getNameDe();
                } else {
                    orgName = org.getNameEn();
                }
                showItems(orgId);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(Const.ORG_PARENT_ID, org.getParentId());
                bundle.putString(Const.ORG_ID, org.getId());

                // set orgName depending on language
                if (languageGerman) {
                    bundle.putString(Const.ORG_NAME, org.getNameDe());
                } else {
                    bundle.putString(Const.ORG_NAME, org.getNameEn());
                }

                // show organisation details
                Intent i = new Intent(OrganisationActivity.this, OrganisationDetailsActivity.class);
                i.putExtras(bundle);
                startActivity(i);

            }
        });
    }
}
