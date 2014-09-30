package de.tum.in.tumcampus.auxiliary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.AccessToken;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

/**
 * Easy accessible class for token management.
 * 
 */
public class AccessTokenManager {
	public static final int MIN_LRZ_LENGTH = 7;

	private final Context context;

	public AccessTokenManager(Context context) {
		this.context = context;
	}

	/**
	 * get a new access token for TUMOnline by passing the lrz ID due to the
	 * simplicity of the given xml file we only need to parse the &lt;token&gt;
	 * element using an xml-parser is simply to much... just extract the pattern
	 * via regex
	 * 
	 * @param lrz_id lrz user id
	 * @return the access token
	 */
    String generateAccessToken(String lrz_id) {
		// we don't have an access token yet, though we take the constructor
		// with only one parameter to set the method
		TUMOnlineRequest<AccessToken> request = new TUMOnlineRequest<AccessToken>(TUMOnlineConst.REQUEST_TOKEN, context, false);
		// add lrz_id to parameters
		request.setParameter("pUsername", lrz_id);
		// add readable name for TUMOnline
		request.setParameter("pTokenName", "TUMCampusApp-" + android.os.Build.PRODUCT);

		// fetch the xml response of requestToken
		AccessToken token = request.fetch();

		// it is only one tag in that xml, let's do a regex pattern
        return token.getToken();
	}

    /**
     * Gets the users lrz id
     * @return LRZ id, if it is set, an empty string otherwise
     */
	private String getLrzId() {
		return Utils.getSetting(context, Const.LRZ_ID);
	}

    /**
     * Test if a valid access token already exists
     * @return True, if access token is set
     */
	public boolean hasValidAccessToken() {
        final String oldAccessToken = Utils.getSetting(context, Const.ACCESS_TOKEN);
        return oldAccessToken != null && oldAccessToken.length() > 2;
	}

	/**
	 * Internal method for setting a new token.
     * It uses the given lrzId to generate a new access token, which is saved to
     * shared preferences afterwards
	 * 
	 * @param lrzId LRZ id
     * @return True if new access token has been set successfully
	 */
	public boolean requestAccessToken(String lrzId) {
		try {
			if (!Utils.isConnected(context)) {
				Utils.showToast(context, R.string.no_internet_connection);
				return false;
			}
			// ok, do the request now
			String strAccessToken = generateAccessToken(lrzId);
			Utils.log("AcquiredAccessToken = " + strAccessToken);

			// save access token to preferences
			Utils.setSetting(context, Const.ACCESS_TOKEN, strAccessToken);
			return true;

		} catch (Exception ex) {
            Utils.log(ex, context.getString(R.string.access_token_wasnt_generated));
			// set access token to null
			Utils.setSetting(context, Const.ACCESS_TOKEN, null);
			Utils.showToast(context, R.string.access_token_wasnt_generated);
		}
		return false;
	}

    /**
     * Generates an access token and if there already is an access token a dialog is shown which
     * asks the user if he wants to generate a new one
     */
	public void setupAccessToken() {
        String lrzId = Utils.getSetting(context, Const.LRZ_ID);
		// check if lrz could be valid?
		if (lrzId.length() == MIN_LRZ_LENGTH) {
			// is access token already set?
            if (hasValidAccessToken()) {
				// show Dialog first
				new AlertDialog.Builder(context)
                        .setMessage(context.getString(R.string.dialog_new_token))
						.setPositiveButton(context.getString(R.string.yes),
								new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        requestAccessToken(getLrzId());
                                    }
                                })
						.setNegativeButton(context.getString(R.string.no), null)
						.show();
			} else {
				requestAccessToken(lrzId);
			}
		} else {
			Utils.showToast(context, R.string.error_lrz_wrong);
		}
	}
}
