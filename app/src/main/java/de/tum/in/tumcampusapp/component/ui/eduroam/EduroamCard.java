package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * Card that can start {@link SetupEduroamActivity}
 */
public class EduroamCard extends Card {

    public EduroamCard(Context context) {
        super(CardManager.CARD_EDUROAM, context, "card_eduroam");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_eduroam, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        MaterialButton button = viewHolder.itemView.findViewById(R.id.eduroam_action_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SetupEduroamActivity.class);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        // Check if WiFi is turned on at all, as we cannot say if it was configured if it is off
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null
                && wifiManager.isWifiEnabled()
                && EduroamController.getEduroamConfig(getContext()) == null
                && eduroamAvailable(wifiManager);
    }

    private boolean eduroamAvailable(@NonNull WifiManager wifi) {
        int fineLocationPermission = ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        int coarseLocationPermission = ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED
                || coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            for (ScanResult scan : wifi.getScanResults()) {
                if (scan.SSID.equals(Const.EDUROAM_SSID)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void discard(@NonNull SharedPreferences.Editor editor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit()
             .putBoolean("card_eduroam_start", false)
             .apply();
    }

    @Override
    public int getId() {
        return 5000;
    }

}
