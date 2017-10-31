package de.tum.in.tumcampusapp.activities.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Optional;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.IdentitySet;
import de.tum.in.tumcampusapp.models.tumo.Person;
import de.tum.in.tumcampusapp.models.tumo.PersonList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 *
 */
public class WizNavCheckTokenActivity extends ActivityForLoadingInBackground<Void, Integer> {

    public WizNavCheckTokenActivity() {
        super(R.layout.activity_wiznav_checktoken);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableRefresh();
    }

    /**
     * If back key is pressed start previous activity
     */
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, WizNavStartActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Open next activity on skip
     *
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        finish();
        startActivity(new Intent(this, WizNavExtrasActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * If next is pressed, check if token has been activated
     *
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }
        startLoading();
    }

    /**
     * Check in background if token has been enabled and get identity for enabling chat
     */
    @Override
    protected Integer onLoadInBackground(Void... arg) {
        // Check if token has been enabled
        if (TUMOnlineRequest.checkTokenInactive(this)) {
            if (NetUtils.isConnected(this)) {
                return R.string.token_not_enabled;
            } else {
                return R.string.no_internet_connection;
            }
        } else {
            // Get users full name
            TUMOnlineRequest<IdentitySet> request2 = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getIDENTITY(), this, true);
            Optional<IdentitySet> id = request2.fetch();
            if (!id.isPresent()) {
                return R.string.no_rights_to_access_id;
            }

            // Save the name to preferences
            Utils.setSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, id.get()
                                                                   .toString());

            // Save the TUMOnline id to preferences
            String pID = getUserPIdentNr(id.get()
                                           .toString());
            if (pID != null) {
                Utils.setSetting(this, Const.TUMO_PIDENT_NR, pID);
            }
            return null;
        }
    }

    /**
     * If everything worked, start the next activity page
     * otherwise give the user the possibility to retry
     */
    @Override
    protected void onLoadFinished(Integer errorMessageStrResId) {
        if (errorMessageStrResId == null) {
            finish();
            startActivity(new Intent(this, WizNavExtrasActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            Utils.showToast(this, errorMessageStrResId);
            showLoadingEnded();
        }
    }

    /**
     * Adds clickable link to activity
     */
    @Override
    protected void onStart() {
        super.onStart();
        TextView textView = findViewById(R.id.tvBrowse);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Get the user's pident nr to identify him a little bit more. This allows information equal to
     * the PersonDetailsActivity
     *
     * @param name The users full name
     * @return the users pID, or null
     */
    @Nullable
    private String getUserPIdentNr(String name) {
        TUMOnlineRequest<PersonList> request = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getPERSON_SEARCH(), this, true);
        request.setParameter("pSuche", name);

        Optional<PersonList> result = request.fetch();

        if (result.isPresent() && result.get()
                                        .getPersons() != null) {
            List<Person> persons = result.get()
                                         .getPersons();

            // Since we can't search by LRZ-Id, we can only search by name, which isn't necessarily
            // unique. We'll probably end up with ubiquitous "Anna Meier"s etc. Only if we are
            // completely certain, display the image rather than displaying a random image
            if (persons.size() == 1) {
                return persons.get(0)
                              .getId();
            }
        }

        return null;
    }
}
