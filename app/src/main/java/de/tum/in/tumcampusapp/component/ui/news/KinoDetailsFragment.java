package de.tum.in.tumcampusapp.component.ui.news;

import android.content.ActivityNotFoundException;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoLocalRepository;
import de.tum.in.tumcampusapp.component.ui.news.repository.KinoRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment {

    private Context context;
    private Kino kino;
    private String url; // link to homepage
    private LayoutInflater inflater;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        KinoLocalRepository.db = TcaDb.getInstance(context);
        KinoViewModel kinoViewModel = new KinoViewModel(KinoLocalRepository.INSTANCE, KinoRemoteRepository.INSTANCE, disposable);
        context = root.getContext();

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

        // initialize all buttons
        Button date = headerView.findViewById(R.id.button_date);
        Button link = headerView.findViewById(R.id.button_link);
        Button imdb = headerView.findViewById(R.id.button_imdb);
        Button year = headerView.findViewById(R.id.button_year);
        Button runtime = headerView.findViewById(R.id.button_runtime);
        Button trailer = headerView.findViewById(R.id.button_trailer);
        ImageView cover = headerView.findViewById(R.id.kino_cover);
        ProgressBar progress = headerView.findViewById(R.id.kino_cover_progress);
        View error = headerView.findViewById(R.id.kino_cover_error);

        // set value for buttons
        date.setText(KinoDetailsFragment.formDateString(DateUtils.getDateString(kino.getDate())));
        imdb.setText(kino.getRating() + " / 10");
        year.setText(kino.getYear());
        runtime.setText(kino.getRuntime());

        // onClickListeners
        link.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        year.setOnClickListener(view -> Toast.makeText(context, R.string.year, Toast.LENGTH_SHORT).show());
        trailer.setOnClickListener(view -> showTrailer());

        // cover
        Picasso.get()
                .load(kino.getCover())
                .into(cover, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress.setVisibility(View.GONE);
                        error.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(Exception e) {
                        progress.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });

        rootView.addView(headerView);
    }

    public void showTrailer() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=" + getTrailerSearchString()));
        try {
            context.startActivity(webIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, getString(R.string.show_trailer_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String getTrailerSearchString(){
        String search = kino.getTitle();
        search = search.split(": ")[1];
        search = "trailer " + search;
        if(!search.contains("OV")){
            search += " german deutsch";
        }
        search = search.replace(' ', '+');

        return search;
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
