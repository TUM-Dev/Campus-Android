package de.tum.in.tumcampusapp.component.ui.plan;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
import de.tum.in.tumcampusapp.component.ui.plan.PlanListAdapter.PlanListEntry;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment that shows a list of available plans.
 */
public class PlansViewFragment extends Fragment {

    /**
     * An enum to map urls to local filenames
     */
    private enum PlanFile {
        SCHNELLBAHNNETZ("https://www.mvv-muenchen.de/fileadmin/mediapool/03-Plaene_Bahnhoefe/Netzplaene/2018_MVVnetz_Final_S_U_R_T_screen.pdf", "Schnellbahnnetz2018.pdf"),
        NACHTLINIENNETZ("https://www.mvv-muenchen.de/fileadmin/mediapool/03-Plaene_Bahnhoefe/Netzplaene/Nachtnetz_2018.pdf", "Nachtliniennetz2018.pdf"),
        TRAMNETZ("https://www.mvv-muenchen.de/fileadmin/mediapool/03-Plaene_Bahnhoefe/Netzplaene/Tramnetz2018_screen.pdf", "Tramnetz2018.pdf"),
        GESAMTNETZ("https://www.mvv-muenchen.de/fileadmin/mediapool/03-Plaene_Bahnhoefe/Tarifplaene/TARIFPLAN_Gesamtnetz_2018.PDF", "Gesamtnetz2018.pdf");

        private final String localName;
        private final String url;

        PlanFile(String url, String localName) {
            this.url = url;
            this.localName = localName;
        }

        public String getLocalName() {
            return this.localName;
        }

        public String getUrl() {
            return this.url;
        }
    }

    private final LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);

    private String fileDirectory;
    private PlanListAdapter mListAdapter;
    private ListView list;

    private ProgressBar progressBar;

    /**
     * The actual downloading of the pdf files occurs here. Until the download process is finished
     * the listview is disabled and the progressbar is shown.
     */
    private void downloadAll() {
        final NetUtils netUtils = new NetUtils(getContext());
        final PlanFile[] files = PlanFile.values();
        final int progressPerFile = 100 / files.length;
        Observable.fromArray(files)
                  .compose(provider.bindToLifecycle())
                  .subscribeOn(Schedulers.io())
                  .zipWith(Observable.range(1, files.length), (file, i) -> {
                      try {
                          String localFile = fileDirectory + '/' + file.getLocalName();
                          netUtils.downloadToFile(file.getUrl(), localFile);
                      } catch (IOException e) {
                          Utils.log(e);
                      }
                      return i;
                  })
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(i -> {
                      if (i < files.length) {
                          progressBar.setProgress(i * progressPerFile);
                      } else {
                          progressBar.setVisibility(View.GONE);
                          list.setEnabled(true);
                      }
                  });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileDirectory = getContext().getApplicationContext()
                                    .getFilesDir()
                                    .getPath();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_plans_view, container, false);
        progressBar = fragmentView.findViewById(R.id.progressBar2);

        list = fragmentView.findViewById(R.id.activity_plans_list_view);
        List<PlanListEntry> listMenuEntrySet = ImmutableList.<PlanListEntry>builder()
                .add(new PlanListEntry(R.drawable.plan_mvv_icon, R.string.mvv_fast_train_net, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.plan_mvv_night_icon, R.string.mvv_nightlines, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.plan_tram_icon, R.string.mvv_tram, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.mvv_entire_net_icon, R.string.mvv_entire_net, R.string.empty_string, 0))
                .add(new PlanListEntry(R.drawable.plan_campus_garching_icon, R.string.campus_garching, R.string.campus_garching_adress, R.drawable.campus_garching))
                .add(new PlanListEntry(R.drawable.plan_campus_klinikum_icon, R.string.campus_klinikum, R.string.campus_klinikum_adress, R.drawable.campus_klinikum))
                .add(new PlanListEntry(R.drawable.plan_campus_olympiapark_icon, R.string.campus_olympiapark, R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark))
                .add(new PlanListEntry(R.drawable.plan_campus_olympiapark_hallenplan_icon, R.string.campus_olympiapark_gyms, R.string.campus_olympiapark_adress, R.drawable.campus_olympiapark_hallenplan))
                .add(new PlanListEntry(R.drawable.plan_campus_stammgelaende__icon, R.string.campus_main, R.string.campus_main_adress, R.drawable.campus_stammgelaende))
                .add(new PlanListEntry(R.drawable.plan_campus_weihenstephan_icon, R.string.campus_weihenstephan, R.string.campus_weihenstephan_adress, R.drawable.campus_weihenstephan))
                .build();

        //Check if there are any new files to download
        downloadFiles();

        //Add files/links to listview
        mListAdapter = new PlanListAdapter(listMenuEntrySet);
        list.setAdapter(mListAdapter);
        list.setOnItemClickListener((adapterView, view, pos, id) -> {
            PlanListEntry entry = (PlanListEntry) mListAdapter.getItem(pos);
            if (pos <= 3) {
                String currentLocalName = PlanFile.values()[pos].getLocalName();
                File pdfFile = new File(fileDirectory, currentLocalName);
                if (pdfFile.exists()) {
                    Intent intent = new Intent(getContext(), PDFViewActivity.class);
                    intent.putExtra(Const.PDF_TITLE, getString(entry.titleId));
                    intent.putExtra(Const.PDF_PATH, pdfFile.getAbsolutePath());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "File doesn't exist yet...did you download it?", Toast.LENGTH_LONG)
                         .show();
                    downloadFiles();
                }
            } else {
                Intent intent = new Intent(getContext(), PlansDetailsActivity.class);
                intent.putExtra(PlansDetailsActivity.PLAN_TITLE_ID, entry.titleId);
                intent.putExtra(PlansDetailsActivity.PLAN_IMG_ID, entry.imgId);
                startActivity(intent);
            }
        });
        return fragmentView;
    }

    private void downloadFiles() {
        for (PlanFile file : PlanFile.values()) {
            Utils.log(fileDirectory + "/" + file.getLocalName());
            if (!(new File(fileDirectory + "/" + file.getLocalName())).exists()) {
                displayDownloadDialog();
                break;
            }
        }
    }

    private void displayDownloadDialog() {
        final Intent back_intent = new Intent(getContext(), MainActivity.class);
        new AlertDialog.Builder(getContext())
                .setTitle("MVV plans")
                .setMessage(getResources().getString(R.string.mvv_download))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> startActivity(back_intent))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    list.setEnabled(false);
                    downloadAll();
                })
                .show();
    }

}

