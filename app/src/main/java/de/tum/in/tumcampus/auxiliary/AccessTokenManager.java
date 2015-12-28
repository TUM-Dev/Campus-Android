package de.tum.in.tumcampus.auxiliary;

import android.app.Activity;
import android.content.Context;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.AccessToken;
import de.tum.in.tumcampus.tumonline.TUMOException;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

/**
 * Easy accessible class for token management.
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
    String generateAccessToken(String lrz_id) throws TUMOException {
        // we don't have an access token yet, though we take the constructor
        // with only one parameter to set the method
        TUMOnlineRequest<AccessToken> request = new TUMOnlineRequest<>(TUMOnlineConst.REQUEST_TOKEN, context, false);

        // add lrz_id to parameters
        request.setParameter("pUsername", lrz_id);

        // add readable name for TUMOnline
        request.setParameter("pTokenName", "TUMCampusApp-" + android.os.Build.PRODUCT);

        // fetch the xml response of requestToken
        AccessToken token = request.fetch();

        //Try to get the token
        if (token == null) {
            throw new TUMOException(request.getLastError());
        }
        return token.getToken();
    }

    /**
     * Test if a valid access token already exists
     *
     * @return True, if access token is set
     */
    public boolean hasValidAccessToken() {
        final String oldAccessToken = Utils.getSetting(context, Const.ACCESS_TOKEN, "");
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
    public boolean requestAccessToken(Activity activity, String lrzId) {
        try {
            if (!NetUtils.isConnected(context)) {
                Utils.showToastOnUIThread(activity, R.string.no_internet_connection);
                return false;
            }
            // ok, do the request now
            String strAccessToken = generateAccessToken(lrzId);
            Utils.log("AcquiredAccessToken = " + strAccessToken);

            // save access token to preferences
            Utils.setSetting(context, Const.ACCESS_TOKEN, strAccessToken);
            return true;

        } catch (TUMOException ex) {
            Utils.log(ex, context.getString(R.string.access_token_wasnt_generated) + ex.errorMessage);
            // set access token to null
            Utils.setSetting(context, Const.ACCESS_TOKEN, null);

            Utils.showToastOnUIThread(activity, ex.errorMessage);
        } catch (Exception ex) {
            Utils.log(ex, context.getString(R.string.access_token_wasnt_generated));
            // set access token to null
            Utils.setSetting(context, Const.ACCESS_TOKEN, null);

            Utils.showToastOnUIThread(activity, R.string.access_token_wasnt_generated);
        }
        return false;
    }
}
