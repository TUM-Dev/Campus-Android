package de.tum.in.tumcampusapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.models.tumcabe.Kino;
import de.tum.in.tumcampusapp.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.viewmodel.KinoViewModel;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment implements View.OnClickListener {

    private Context context;
    private Kino kino;
    private NetUtils net;
    private String url; // link to homepage
    private LayoutInflater inflater;

    private KinoViewModel kinoViewModel;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        KinoLocalRepository.db = TcaDb.getInstance(context);
        kinoViewModel = new KinoViewModel(KinoLocalRepository.INSTANCE, KinoRemoteRepository.INSTANCE, disposable);
        context = root.getContext();
        net = new NetUtils(context);

        kinoViewModel.getKinoByPosition(position)
                     .subscribe(kino1 -> {
                         kino = kino1;
                         showDetails(root);
                     });

        return rootView;
    }

    /**
     * creates the content of the fragment
     *
     * @param rootView view on which the content gets drawn
     */
    private void showDetails(LinearLayout rootView) {
        url = kino.getLink();

        createKinoHeader(rootView);
        createKinoFooter(rootView);
    }

    private void addToRoot(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        rootView.addView(view);
    }

    private void addToRootWithPadding(LinearLayout rootView, int headerId, CharSequence contentString) {
        View view = inflater.inflate(R.layout.list_header_big, rootView, false);
        TextView text = view.findViewById(R.id.list_header);
        text.setText(headerId);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(contentString);
        // padding is done programmatically here because we need more padding at the end
        int padding = (int) context.getResources()
                                   .getDimension(R.dimen.padding_kino);
        int paddingRight = (int) context.getResources()
                                        .getDimension(R.dimen.padding_kino_right);
        int paddingEnd = (int) context.getResources()
                                      .getDimension(R.dimen.padding_kino_end);
        text.setPadding(padding, padding, paddingRight, paddingEnd);
        rootView.addView(view);
    }

    private void createKinoFooter(LinearLayout root) {
        addToRoot(root, R.string.genre, kino.getGenre());
        addToRoot(root, R.string.director, kino.getDirector());
        addToRoot(root, R.string.actors, kino.getActors());
        addToRootWithPadding(root, R.string.description, kino.getDescription());
    }

    private void createKinoHeader(LinearLayout rootView) {
        LinearLayout headerView = (LinearLayout) inflater.inflate(R.layout.kino_header, rootView, false);

        Button date;
        Button link;
        Button imdb;
        Button year;
        Button runtime;
        ImageView cover = headerView.findViewById(R.id.kino_cover);

        // initialize all buttons
        date = headerView.findViewById(R.id.button_date);
        link = headerView.findViewById(R.id.button_link);
        imdb = headerView.findViewById(R.id.button_imdb);
        year = headerView.findViewById(R.id.button_year);
        runtime = headerView.findViewById(R.id.button_runtime);

        // set text for all buttons
        date.setText(KinoDetailsFragment.formDateString(Utils.getDateString(kino.getDate())));
        link.setText(R.string.www);
        imdb.setText(kino.getRating());
        year.setText(kino.getYear());
        runtime.setText(kino.getRuntime());

        // set onClickListener
        date.setOnClickListener(this);
        link.setOnClickListener(this);
        imdb.setOnClickListener(this);
        year.setOnClickListener(this);
        runtime.setOnClickListener(this);

        // cover
        net.loadAndSetImage(kino.getCover(), cover);

        rootView.addView(headerView);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.button_date) {
            Toast.makeText(context, R.string.date, Toast.LENGTH_SHORT)
                 .show();
        } else if (i == R.id.button_link) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else if (i == R.id.button_imdb) {
            Toast.makeText(context, R.string.imdb_rating, Toast.LENGTH_SHORT)
                 .show();
        } else if (i == R.id.button_year) {
            Toast.makeText(context, R.string.year, Toast.LENGTH_SHORT)
                 .show();
        } else if (i == R.id.button_runtime) {
            Toast.makeText(context, R.string.runtime, Toast.LENGTH_SHORT)
                 .show();
        }
    }

    /**
     * formats the dateString
     *
     * @param date Date string stored in database
     * @return formated dateString
     */
    private static CharSequence formDateString(String date) {
        return date.substring(8, 10) + '.' + date.substring(5, 7) + '.';
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
