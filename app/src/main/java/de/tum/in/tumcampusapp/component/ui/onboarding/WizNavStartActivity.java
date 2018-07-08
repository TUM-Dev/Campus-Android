package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.app.exception.NoPublicKey;
import de.tum.in.tumcampusapp.api.tumonline.AccessTokenManager;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.api.tumonline.exception.InactiveTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.InvalidTokenException;
import de.tum.in.tumcampusapp.api.tumonline.exception.RequestLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.TokenLimitReachedException;
import de.tum.in.tumcampusapp.api.tumonline.exception.UnknownErrorException;
import de.tum.in.tumcampusapp.api.tumonline.model.AccessToken;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the first page of the startup wizard, where the user can enter his lrz-id.
 */
public class WizNavStartActivity
        extends ActivityForAccessingTumOnline implements OnClickListener, TextWatcher {

    private final AccessTokenManager accessTokenManager = new AccessTokenManager(this);
    private String lrzId;

    private EditText lrzIdEditText;
    private AppCompatButton nextButton;

    public WizNavStartActivity() {
        super(R.layout.activity_wiznav_start);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disableRefresh();
        findViewById(R.id.wizard_start_layout).requestFocus();

        nextButton = findViewById(R.id.next_button);

        lrzIdEditText = findViewById(R.id.lrz_id);
        lrzIdEditText.addTextChangedListener(this);
        lrzIdEditText.setText(Utils.getSetting(this, Const.LRZ_ID, ""));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Make sure to delete information that might lead the app to believe the user enabled TUMonline
        Utils.setSetting(this, Const.LRZ_ID, null);
        Utils.setSetting(this, Const.ACCESS_TOKEN, null);
    }

    /**
     * Handle click on next button.
     *
     * @param next Next button handle
     */
    public void onClickNext(View next) {
        String enteredId = lrzIdEditText.getText().toString().toLowerCase(Locale.GERMANY);

        // check if lrz could be valid?
        if (!enteredId.matches(Const.TUM_ID_PATTERN)) {
            Utils.showToast(this, R.string.error_invalid_tum_id);
            return;
        }

        lrzId = enteredId;
        Utils.setSetting(this, Const.LRZ_ID, lrzId);

        // is access token already set?
        if (accessTokenManager.hasValidAccessToken()) {
            // show Dialog first
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.error_access_token_already_set_generate_new))
                    .setPositiveButton(getString(R.string.generate_new_token), this)
                    .setNegativeButton(getString(R.string.cancel), this)
                    .show();
        } else {
            requestNewToken(lrzId);
        }
    }

    // TODO TILL: Implement retry method

    private void requestNewToken(String publicKey) {
        showLoadingStart();
        String tokenName = "TUMCampusApp-" + Build.PRODUCT;
        TUMOnlineClient
                .getInstance(this)
                .requestToken(publicKey, tokenName)
                .enqueue(new Callback<AccessToken>() {
                    @Override
                    public void onResponse(@NonNull Call<AccessToken> call,
                                           @NonNull Response<AccessToken> response) {
                        AccessToken accessToken = response.body();
                        if (accessToken != null) {
                            handleTokenDownloadSuccess(accessToken);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                        handleTokenDownloadFailure(t);
                    }
                });
    }

    private void handleTokenDownloadSuccess(AccessToken accessToken) {
        Utils.log("AcquiredAccessToken = " + accessToken.getToken());

        // Save access token to preferences
        Utils.setSetting(this, Const.ACCESS_TOKEN, accessToken.getToken());

        // Upload the secret to this new generated token
        AuthenticationManager am = new AuthenticationManager(this);
        try {
            am.uploadPublicKey();
        } catch (NoPublicKey noPublicKey) {
            Utils.log(noPublicKey);
        }

        openNextWizardStep();
    }

    private void handleTokenDownloadFailure(Throwable throwable) {
        Utils.log(throwable);

        int messageResId;

        if (throwable instanceof InactiveTokenException) {
            messageResId = R.string.error_access_token_inactive;
        } else if (throwable instanceof InvalidTokenException) {
            messageResId = R.string.error_invalid_access_token;
        } else if (throwable instanceof UnknownErrorException) {
            messageResId = R.string.error_unknown;
        } else if (throwable instanceof TokenLimitReachedException) {
            messageResId = R.string.error_access_token_limit_reached;
        } else if (throwable instanceof RequestLimitReachedException) {
            messageResId = R.string.error_request_limit_reached;
        } else {
            messageResId = R.string.error_access_token_could_not_be_generated;
        }

        // Set access token to null
        Utils.setSetting(this, Const.ACCESS_TOKEN, null);
        showLoadingEnded();
        displayErrorDialog(messageResId);
    }

    private void displayErrorDialog(int messageResId) {
        hideKeyboard();
        new AlertDialog.Builder(this)
                .setMessage(messageResId)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(lrzIdEditText.getWindowToken(), 0);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean isEmpty = s.toString().trim().isEmpty();
        float alpha = (isEmpty) ? 0.5f : 1.0f;
        nextButton.setClickable(!isEmpty);
        nextButton.setAlpha(alpha);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Pass
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Pass
    }

    /**
     * Handle click in dialog buttons.
     *
     * @param dialog Dialog handle
     * @param which  Button clicked
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            AuthenticationManager am = new AuthenticationManager(this);
            am.clearKeys();
            am.generatePrivateKey(null);
            requestNewToken(lrzId);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            openNextWizardStep();
        }
    }

    private void openNextWizardStep() {
        startActivity(new Intent(this, WizNavCheckTokenActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    protected void onStop() {
        super.onStop();
        lrzIdEditText.removeTextChangedListener(this);
    }

}