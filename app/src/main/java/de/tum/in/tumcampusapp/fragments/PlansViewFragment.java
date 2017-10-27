package de.tum.in.tumcampusapp.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.MainActivity;
import de.tum.in.tumcampusapp.activities.PlansDetailsActivity;
import de.tum.in.tumcampusapp.adapters.PlanListAdapter;
import de.tum.in.tumcampusapp.adapters.PlanListAdapter.PlanListEntry;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;

public class PlansViewFragment extends Fragment {

    /**
     * An enum to map urls to local filenames
     */
    private enum PlanFile {
        SCHNELLBAHNNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Netz_2017_Version_EFA-2.PDF", "Schnellbahnnetz2017.pdf"),
        NACHTLINIENNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Nachtnetz_2017.pdf", "Nachtliniennetz2017.pdf"),
        TRAMNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/plaene/pdf/Tramnetz_2017.pdf", "Tramnetz2017.pdf"),
        GESAMTNETZ("http://www.mvv-muenchen.de/fileadmin/media/Dateien/3_Tickets_Preise/dokumente/TARIFPLAN_2017-Gesamtnetz.PDF", "Gesamtnetz2017.pdf");

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

    private String fileDirectory;
    private PlanListAdapter mListAdapter;
    private ListView list;

    private ProgressBar progressBar;

    /**
     * The actual downloading of the pdf files occurs here. Until the download process is finished
     * the listview is disabled and the progressbar is shown.
     */
    private final AsyncTask<PlanFile, Integer, Void> pdfDownloader = new AsyncTask<PlanFile, Integer, Void>() {
        @Override
        protected Void doInBackground(PlanFile... files) {
            Utils.log("Starting download.");
            NetUtils netUtils = new NetUtils(getContext().getApplicationContext());
            int progressPerFile = 100 / files.length;
            int i = 0;
            for (PlanFile file : files) {
                try {
                    String localFile = fileDirectory + '/' + file.getLocalName();
                    netUtils.downloadToFile(file.getUrl(), localFile);
                    publishProgress((++i) * progressPerFile);
                    Utils.log(localFile);
                } catch (IOException e) {
                    Utils.log(e);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void v) {
            progressBar.setVisibility(View.GONE);
            list.setEnabled(true);
        }
    };

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
        mListAdapter = new PlanListAdapter(getActivity(), listMenuEntrySet);
        list.setAdapter(mListAdapter);
        list.setOnItemClickListener((adapterView, view, pos, id) -> {
            PlanListEntry entry = (PlanListEntry) mListAdapter.getItem(pos);
            if (pos <= 3) {
                String currentLocalName = PlanFile.values()[pos].getLocalName();
                File pdfFile = new File(fileDirectory, currentLocalName);
                if (pdfFile.exists()) {
                    if (!openPdfViewer(pdfFile)) {
                        Toast.makeText(getContext(), "Invalid file format, please let us know of this bug - plans have probably been updated.", Toast.LENGTH_LONG)
                             .show();
                    }
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
                    pdfDownloader.execute(PlanFile.values());
                    list.setEnabled(false);
                })
                .show();
    }

    /**
     * Either creates a new one or uses a existing PdfViewFragment. Then the method tries to open
     * a given file in the acquired pdf fragment. If that's successful, the current fragment gets
     * added to the backstack and is being replaced by the pdf fragment.
     *
     * @param pdf The file to be opened
     * @return True, if opening the Pdf was successful, False otherwise. (e.g. file was not pdf but 404 html instead)
     */
    public boolean openPdfViewer(File pdf) {
        PdfViewFragment pdfFragment = (PdfViewFragment) getActivity().getSupportFragmentManager()
                                                                     .findFragmentByTag("PDF_FRAGMENT");
        if (pdfFragment == null) {
            pdfFragment = new PdfViewFragment();
        }
        if (!pdfFragment.setPdf(pdf)) {
            return false;
        }
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                                                       .beginTransaction();
        transaction.replace(R.id.activity_plans_fragment_frame, pdfFragment, "PDF_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
        return true;
    }
}

