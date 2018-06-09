package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
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
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class EduroamFixCard extends Card {

    private static final String RADIUS_DNS = "radius.lrz.de";
    private final List<String> errors;
    private WifiConfiguration eduroam;
    private static final String AT_SIGN = "@";

    public EduroamFixCard(Context context) {
        super(CardManager.CARD_EDUROAM_FIX, context, "card_eduroam_fix_start");
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
        setMCard(viewHolder.itemView);
        setMLinearLayout(getMCard().findViewById(R.id.card_view));
        TextView errorsTv = getMCard().findViewById(R.id.eduroam_errors);
        errorsTv.setText(Joiner.on("\n").join(errors));

        // only error is missing realm which is not insecure per se but also not right
        if (errors.size() == 1 && errors.get(0)
                                        .equals(getContext().getString(R.string.wifi_identity_zone))) {
            getMCard().findViewById(R.id.eduroam_insecure_message)
                      .setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        //Check if wifi is turned on at all, as we cannot say if it was configured if its off
        WifiManager wifi = (WifiManager) getContext().getApplicationContext()
                                                     .getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            return false;
        }

        return !isConfigValid();
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit()
             .putBoolean("card_eduroam_fix_start", false)
             .apply();
    }

    @Override
    public Intent getIntent() {
        if (eduroam != null) {
            WifiManager wifi = (WifiManager) getContext().getApplicationContext()
                                                         .getSystemService(Context.WIFI_SERVICE);
            wifi.removeNetwork(eduroam.networkId);
        }
        Intent intent = new Intent(getContext(), SetupEduroamActivity.class);
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
        eduroam = EduroamController.getEduroamConfig(getContext());

        //If it is not configured then the config valid
        if (eduroam == null) {
            return true;
        }

        // Eduroam was configured by other university
        if (!isTumEduroam(eduroam.enterpriseConfig.getIdentity())) {
            Utils.log("Eduroam wasn't configured at TUM");
            return true;
        }

        // Check attributes - check newer match for the radius server
        // for all configurations
        // Check that the full quantifier is used (we already know it's a tum config)
        if (!eduroam.enterpriseConfig.getIdentity().contains(AT_SIGN)) {
            errors.add(getContext().getString(R.string.wifi_identity_zone));
        }

        int eapMethod = eduroam.enterpriseConfig.getEapMethod();
        int phase2 = eduroam.enterpriseConfig.getPhase2Method();

        if (eapMethod == WifiEnterpriseConfig.Eap.TTLS &&
            (phase2 == WifiEnterpriseConfig.Phase2.MSCHAPV2 || phase2 == WifiEnterpriseConfig.Phase2.PAP)
            || eapMethod == WifiEnterpriseConfig.Eap.PEAP && phase2 == WifiEnterpriseConfig.Phase2.MSCHAPV2) {

            checkDNSName();
            checkAnonymousIdentity();
            // note: checking the certificate does not seem possible
        }
        // else: PWD or unknown authentication method (we don't know if that method is safe or not -> ignore)

        return errors.isEmpty();
    }

    private void checkAnonymousIdentity() {
        String anonymousIdentity = eduroam.enterpriseConfig.getAnonymousIdentity();
        if (anonymousIdentity != null
            && !anonymousIdentity.equals("anonymous@mwn.de")
                && !anonymousIdentity.equals("anonymous@eduroam.mwn.de")
                && !anonymousIdentity.equals("anonymous@mytum.de")) {
            errors.add(getContext().getString(R.string.wifi_anonymous_identity_not_set));
        }
    }

    private void checkDNSName() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !isValidSubjectMatchAPI18(eduroam)) {
            errors.add(getContext().getString(R.string.wifi_dns_name_not_set));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                   && (!eduroam.enterpriseConfig.getAltSubjectMatch().equals("DNS:" + RADIUS_DNS)
                       || !eduroam.enterpriseConfig.getDomainSuffixMatch().equals(RADIUS_DNS))
                   && !isValidSubjectMatchAPI18(eduroam)) {
            errors.add(getContext().getString(R.string.wifi_dns_name_not_set));
        }
    }

    private boolean isTumEduroam(String identity) {
        Pattern pattern = Pattern.compile(Const.TUM_ID_PATTERN);
        return identity.endsWith("@mwn.de")
               || identity.endsWith("@mytum.de")
               || identity.endsWith("@tum.de")
               || (identity.endsWith(".mwn.de") || identity.endsWith(".tum.de")) && identity.contains(AT_SIGN)
               || pattern.matcher(identity).matches();
    }

    private boolean isValidSubjectMatchAPI18(WifiConfiguration eduroam) {
        Utils.log("SubjectMatch: " + eduroam.enterpriseConfig.getSubjectMatch());
        return eduroam.enterpriseConfig.getSubjectMatch().equals(RADIUS_DNS);
    }
}
