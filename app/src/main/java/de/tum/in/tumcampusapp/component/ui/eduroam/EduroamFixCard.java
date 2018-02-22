package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.component.ui.eduroam.EduroamController.RADIUS_DNS;

public class EduroamFixCard extends NotificationAwareCard {

    private final List<String> errors;
    private TextView errorsTv;
    private WifiConfiguration eduroam;
    private static final String atSign = "@";

    public EduroamFixCard(Context context) {
        super(CardManager.CARD_EDUROAM_FIX, context, "card_eduroam_fix_start", false, true);
        errors = new ArrayList<>();
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_eduroam_fix, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = mCard.findViewById(R.id.card_view);
        errorsTv = mCard.findViewById(R.id.eduroam_errors);
        errorsTv.setText(Joiner.on("\n")
                               .join(errors));

        // only error is missing realm which is not insecure per se but also not right
        if(errors.size() == 1 && errors.get(0).equals(mContext.getString(R.string.wifi_identity_zone))){
            mCard.findViewById(R.id.eduroam_insecure_message).setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        //Check if wifi is turned on at all, as we cannot say if it was configured if its off
        WifiManager wifi = (WifiManager) mContext.getApplicationContext()
                                                 .getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            return false;
        }

        return !isConfigValid();
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
             .putBoolean("card_eduroam_fix_start", false)
             .apply();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return null;
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.fix_eduroam);
    }

    @Override
    public Intent getIntent() {
        if (eduroam != null) {
            WifiManager wifi = (WifiManager) mContext.getApplicationContext()
                                                     .getSystemService(Context.WIFI_SERVICE);
            wifi.removeNetwork(eduroam.networkId);
        }
        Intent intent = new Intent(mContext, SetupEduroamActivity.class);
        // TCA should only produce correct profiles, so incorrect ones were configured somewhere else
        intent.putExtra(Const.EXTRA_FOREIGN_CONFIGURATION_EXISTS, true);
        return intent;
    }

    @Override
    public int getId() {
        return 0;
    }

    private boolean isConfigValid() {
        errors.clear();
        eduroam = EduroamController.getEduroamConfig(mContext);

        //If it is not configured then the config valid
        if (eduroam == null) {
            return true;
        }

        // Eduroam was configured by other university
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
                && !isTumEduroam(eduroam.enterpriseConfig.getIdentity())) {
            Utils.log("Eduroam wasn't configured at TUM");
            return true;
        }

        // Check attributes - Android 23+: check newer match for the radius server
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // for all configurations
            // Check that the full quantifier is used (we already know it's a tum config)
            if (!eduroam.enterpriseConfig.getIdentity().contains(atSign)) {
                errors.add(mContext.getString(R.string.wifi_identity_zone));
            }

            int eapMethod = eduroam.enterpriseConfig.getEapMethod();
            int phase2 = eduroam.enterpriseConfig.getPhase2Method();

            if ((eapMethod == WifiEnterpriseConfig.Eap.TTLS
                 && (phase2 == WifiEnterpriseConfig.Phase2.MSCHAPV2 || phase2 == WifiEnterpriseConfig.Phase2.PAP)
                 || (eapMethod == WifiEnterpriseConfig.Eap.PEAP
                     && phase2 == WifiEnterpriseConfig.Phase2.MSCHAPV2))) {

                checkDNSName();
                checkAnonymousIdentity();

                // note: checking the certificate does not seem possible
            } else {
                // PWD or unknown authentication method (we don't know if that method is safe or not -> ignore)
            }
        }
        return errors.isEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void checkAnonymousIdentity(){
        String anonymousIdentity = eduroam.enterpriseConfig.getAnonymousIdentity();
        if (anonymousIdentity != null
            && (!anonymousIdentity.equals("anonymous@mwn.de")
                && !anonymousIdentity.equals("anonymous@eduroam.mwn.de")
                && !anonymousIdentity.equals("anonymous@mytum.de"))) {
            errors.add(mContext.getString(R.string.wifi_anonymous_identity_not_set));
        }
    }

    @SuppressLint("NewApi")
    private void checkDNSName(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            && !isValidSubjectMatchAPI18(eduroam)) {
            errors.add(mContext.getString(R.string.wifi_dns_name_not_set));
        } else {
            if ((!eduroam.enterpriseConfig.getAltSubjectMatch().equals("DNS:" + RADIUS_DNS)
                 || !eduroam.enterpriseConfig.getDomainSuffixMatch().equals(RADIUS_DNS))
                && !isValidSubjectMatchAPI18(eduroam)) {
                errors.add(mContext.getString(R.string.wifi_dns_name_not_set));
            } else {
                Utils.log("AltSubjectMatch: " + eduroam.enterpriseConfig.getAltSubjectMatch());
                Utils.log("DomainSuffixMatch: " + eduroam.enterpriseConfig.getDomainSuffixMatch());
            }
        }
    }

    private boolean isTumEduroam(String identity) {
        Pattern pattern = Pattern.compile(Const.TUM_ID_PATTERN);
        return identity.endsWith("@mwn.de") || identity.endsWith("@mytum.de") || identity.endsWith("@tum.de")
                || ((identity.endsWith(".mwn.de") || identity.endsWith(".tum.de")) && identity.contains(atSign))
               || pattern.matcher(identity).matches();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean isValidSubjectMatchAPI18(WifiConfiguration eduroam) {
        Utils.log("SubjectMatch: " + eduroam.enterpriseConfig.getSubjectMatch());
        return eduroam.enterpriseConfig.getSubjectMatch()
                                       .equals(RADIUS_DNS);
    }
}
