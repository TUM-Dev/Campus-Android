package de.tum.in.tumcampusapp.cards;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Vector;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.SetupEduroamActivity;
import de.tum.in.tumcampusapp.cards.generic.NotificationAwareCard;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.managers.EduroamManager;

import static de.tum.in.tumcampusapp.managers.EduroamManager.RADIUS_DNS;


public class EduroamFixCard extends NotificationAwareCard {

    private Vector<String> errors;
    private TextView errorsTv;
    private WifiConfiguration eduroam;

    public EduroamFixCard(Context context) {
        super(CardManager.CARD_EDUROAM_FIX, context, "card_eduroam_fix_start", false, true);
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_eduroam_fix, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        mCard = viewHolder.itemView;
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        errorsTv = (TextView) mCard.findViewById(R.id.eduroam_errors);

        String txt = "";
        for (String e : errors) {
            txt += e + "\n";
        }
        errorsTv.setText(txt);
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        return !isConfigValid();
    }

    @Override
    protected void discard(SharedPreferences.Editor editor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putBoolean("card_eduroam_fix_start", false).apply();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        return null;
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.eduroam_fix);
    }

    @Override
    public Intent getIntent() {
        if (eduroam != null) {
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            wifi.removeNetwork(eduroam.networkId);
        }
        return new Intent(mContext, SetupEduroamActivity.class);
    }

    @Override
    public int getId() {
        return 0;
    }


    private boolean isConfigValid() {
        errors = new Vector<>();
        eduroam = EduroamManager.getEduroamConfig(mContext);

        //If it is not configured then the config valid
        if (eduroam == null) {
            return true;
        }

        //Otherwise check attributes
        //Android 23+: check newer match for the radius server
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (!eduroam.enterpriseConfig.getAltSubjectMatch().equals("DNS:" + RADIUS_DNS) || !eduroam.enterpriseConfig.getDomainSuffixMatch().equals(RADIUS_DNS))
                && !isValidSubjectMatchAPI18(eduroam)) {
            errors.add(mContext.getString(R.string.wifi_dns_name_not_set));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !isValidSubjectMatchAPI18(eduroam)) {
                errors.add(mContext.getString(R.string.wifi_dns_name_not_set));
            }

            //Check that the full quantifier and correct identity is used
            if (!eduroam.enterpriseConfig.getIdentity().contains("@eduroam.mwn.de") && !eduroam.enterpriseConfig.getIdentity().contains("@mytum.de") && !eduroam.enterpriseConfig.getIdentity().contains("@tum.de")) {
                errors.add(mContext.getString(R.string.wifi_identity_zone));
            }
            if (!eduroam.enterpriseConfig.getAnonymousIdentity().equals("anonymous@mwn.de") &&
                    !eduroam.enterpriseConfig.getAnonymousIdentity().equals("anonymous@eduroam.mwn.de") &&
                    !eduroam.enterpriseConfig.getAnonymousIdentity().equals("anonymous@mytum.de")) {
                errors.add(mContext.getString(R.string.wifi_anonymous_identity_not_set));
            }

            //Check certificate
            //TODO
            /*if (eduroam.enterpriseConfig.getCaCertificate() == null) {
                errors.add("No CA certificate set");
            } else {
                Utils.log(eduroam.enterpriseConfig.getCaCertificate().toString());
            }*/

        }

        return errors.size() == 0;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean isValidSubjectMatchAPI18(WifiConfiguration eduroam) {
        return eduroam.enterpriseConfig.getSubjectMatch().equals(RADIUS_DNS);
    }
}
