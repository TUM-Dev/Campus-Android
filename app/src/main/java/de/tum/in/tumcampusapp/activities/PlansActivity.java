package de.tum.in.tumcampusapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.PlanListAdapter;
import de.tum.in.tumcampusapp.adapters.PlanListAdapter.PlanListEntry;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Activity to show plans.
 */
public class PlansActivity extends BaseActivity implements OnItemClickListener {

    private static String[][] filesToDownload = {
            {"http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Netz_2016_Version_MVG.PDF", "Schnellbahnnetz.pdf"},
            {"http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Nachtnetz_2016.pdf", "Nachtliniennetz.pdf"},
            {"http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Tramnetz_2016.pdf", "Tramnetz.pdf"},
            {"http://www.mvv-muenchen.de/fileadmin/media/Dateien/3_Tickets_Preise/dokumente/TARIFPLAN_2016-Innenraum.pdf", "Tarifplan.pdf"},
    };
    ProgressBar progressBar;
    private PlanListAdapter mListAdapter;
    private Thread downloadFiles = new Thread() {
        public void run() {
            Utils.log("Starting download.");
            NetUtils netUtils = new NetUtils(getApplicationContext());

            for (String[] file : PlansActivity.filesToDownload) {
                try {
                    netUtils.downloadToFile(file[0], getApplicationContext().getFilesDir().getPath() + "/" + file[1]);
                    Utils.log(getApplicationContext().getFilesDir() + file[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Finished, notify the UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    };

    public PlansActivity() {
        super(R.layout.activity_plans);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        if (Utils.getInternalSettingInt(this, "mvvplans_downloaded", 0) == 0) {
            final Intent back_intent = new Intent(this, MainActivity.class);

            new AlertDialog.Builder(this)
                    .setTitle("MVV plans")
                    .setMessage(getResources().getString(R.string.mvv_download))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(back_intent);
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            progressBar.setVisibility(View.VISIBLE);
                            downloadFiles.start();
                            Utils.setInternalSetting(getApplicationContext(), "mvvplans_downloaded", 1);
                        }
                    }).show();
        }


        ListView list = (ListView) findViewById(R.id.activity_plans_list_view);
        ArrayList<PlanListEntry> listMenuEntrySet = new ArrayList<>();
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_mvv_icon, R.string.mvv_fast_train_net, R.string.empty_string, R.drawable.mvv));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_mvv_night_icon, R.string.mvv_nightlines, R.string.empty_string, R.drawable.mvv_night));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_tram_icon, R.string.mvv_tram, R.string.empty_string, R.drawable.mvg_tram));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.mvv_entire_net_icon, R.string.mvv_entire_net, R.string.empty_string, R.drawable.mvv_entire_net));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_campus_garching_icon, R.string.campus_garching, R.string.campus_garching_adress, R.drawable.campus_garching));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_campus_klinikum_icon, R.string.campus_klinikum, R.string.campus_klinikum_adress, R.drawable.campus_klinikum));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_campus_olympiapark_icon, R.string.campus_olympiapark, R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_campus_olympiapark_hallenplan_icon, R.string.campus_olympiapark_gyms, R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark_hallenplan));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_campus_stammgelaende__icon, R.string.campus_main, R.string.campus_main_adress, R.drawable.campus_stammgelaende));
        listMenuEntrySet.add(new PlanListEntry(R.drawable.plan_campus_weihenstephan_icon, R.string.campus_weihenstephan, R.string.campus_weihenstephan_adress, R.drawable.campus_weihenstephan));

        mListAdapter = new PlanListAdapter(this, listMenuEntrySet);
        list.setAdapter(mListAdapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        PlanListEntry entry = (PlanListEntry) mListAdapter.getItem(pos);

        if (pos <= 3) {
            File pdfFile = new File(getFilesDir(), PlansActivity.filesToDownload[pos][1]);

            final Uri path = FileProvider.getUriForFile(getApplicationContext(), "de.tum.in.tumcampusapp.fileprovider", pdfFile);

            Intent x = new Intent();
            x.setData(path);
            x.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(RESULT_OK, x);
            startActivity(x);
        } else {
            Intent intent = new Intent(this, PlansDetailsActivity.class);
            intent.putExtra(PlansDetailsActivity.PLAN_TITLE_ID, entry.titleId);
            intent.putExtra(PlansDetailsActivity.PLAN_IMG_ID, entry.imgId);
            startActivity(intent);
        }
    }
}