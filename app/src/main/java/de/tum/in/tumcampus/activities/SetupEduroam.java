package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.EduroamManager;

public class SetupEduroam extends ActionBarActivity {

    private EditText lrz;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_setup_eduroam, null);
        lrz = (EditText) dialogView.findViewById(R.id.wifi_lrz_id);
        lrz.setText(Utils.getSetting(this, Const.LRZ_ID));
        password = (EditText) dialogView.findViewById(R.id.wifi_password);
        password.requestFocus();
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.setup,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EduroamManager manager = new EduroamManager(getApplicationContext());
                        manager.configureEduroam(lrz.getText().toString(), password.getText().toString());
                        // TODO show progress and then success/error message
                        dialog.cancel();
                        SetupEduroam.this.finish();
                    }
                }).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        SetupEduroam.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}