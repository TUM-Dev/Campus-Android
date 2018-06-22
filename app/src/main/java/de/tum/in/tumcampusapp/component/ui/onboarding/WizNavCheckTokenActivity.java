package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.model.DeviceVerification;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.tumui.person.model.Identity;
import de.tum.in.tumcampusapp.component.tumui.person.model.IdentitySet;
import de.tum.in.tumcampusapp.component.tumui.person.model.ObfuscatedIds;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

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
     * If next is pressed, check if token has been activated.
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

    public void onClickTUMOnline(View next) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Const.TUM_CAMPUS_URL));
        startActivity(intent);
    }

    /**
     * Check in background if token has been enabled and get identity for enabling chat.
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
        } else { // Token was activated
            // Get users full name
            TUMOnlineRequest<IdentitySet> request = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getIDENTITY(), this, true);
            Optional<IdentitySet> id = request.fetch();
            if (!id.isPresent()) {
                return R.string.no_rights_to_access_id;
            }

            Identity identity = id.get()
                    .getIds()
                    .get(0);

            // Save the name to preferences
            Utils.setSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, identity
                    .toString());

            // Save the TUMOnline id to preferences
            Utils.setSetting(this, Const.TUMO_PIDENT_NR, identity.getObfuscated_ids()
                    .getStudierende()); // Switch to identity.getObfuscated_id() in the future
            Utils.setSetting(this, Const.TUMO_STUDENT_ID, identity.getObfuscated_ids()
                    .getStudierende());
            Utils.setSetting(this, Const.TUMO_EXTERNAL_ID, identity.getObfuscated_ids()
                    .getExtern());
            Utils.setSetting(this, Const.TUMO_EMPLOYEE_ID, identity.getObfuscated_ids()
                    .getBedienstete());

            String lrzId = Utils.getSetting(this, Const.LRZ_ID, "");
            UploadStatus uploadStatus = TUMCabeClient.getInstance(this)
                    .getUploadStatus(lrzId)
                    .blockingSingle();

            // upload only the ids that haven't been uploaded before
            ObfuscatedIds ids = identity.getObfuscated_ids();
            ObfuscatedIdsUpload upload = prepareIdUpload(this, ids.getStudierende(), ids.getBedienstete(), ids.getExtern(), uploadStatus);
            if (upload != null) {
                TUMCabeClient.getInstance(this).uploadObfuscatedIds(lrzId, upload);
            }

            return null;
        }
    }

    /**
     * @return null if no update is necessary
     */
    public static ObfuscatedIdsUpload prepareIdUpload(Context context, String studentId, String employeeId,
                                                      String externalId, UploadStatus uploadStatus){
        ObfuscatedIdsUpload upload;
        try {
            upload = new ObfuscatedIdsUpload(
                    null, null, null,
                    DeviceVerification.Companion.getDeviceVerification(context));
        } catch (NoPrivateKey noPrivateKey) {
            Utils.log(noPrivateKey, "Can't upload obfuscated ids");
            return null;
        }

        boolean doUpload = false;
        if (!uploadStatus.getStudentId() && !studentId.isEmpty()) {
            upload.setStudentId(studentId);
            doUpload = true;
        }
        if (!uploadStatus.getEmployeeId() && !employeeId.isEmpty()) {
            upload.setEmployeeId(employeeId);
            doUpload = true;
        }
        if (!uploadStatus.getExternalId() && !externalId.isEmpty()) {
            upload.setExternalId(externalId);
            doUpload = true;
        }

        if (doUpload) {
            return upload;
        }
        return null;
    }


    /**
     * If everything worked, start the next activity page
     * otherwise give the user the possibility to retry.
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

}
