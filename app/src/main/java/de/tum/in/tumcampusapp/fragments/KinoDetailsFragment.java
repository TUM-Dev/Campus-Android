package de.tum.in.tumcampusapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import de.tum.in.tumcampusapp.managers.KinoManager;

/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment implements View.OnClickListener {

    private Context context;
    private Cursor cursor;
    private NetUtils net;
    private String url; // link to homepage

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);

        // position in database
        int position = getArguments().getInt(Const.POSITION);

        context = root.getContext();
        cursor = new KinoManager(context).getAllFromDb();
        net = new NetUtils(context);

        showDetails(root, position);
        return rootView;
    }

    /**
     * creates the content of the fragment
     *
     * @param rootView view on which the content gets drawn
     * @param position position in database
     */
    private void showDetails(LinearLayout rootView, int position) {
        cursor.moveToPosition(position);
        url = cursor.getString(cursor.getColumnIndex(Const.JSON_LINK));

        createKinoHeader(rootView);
        createKinoFooter(rootView);

        cursor.close();

    }

    private void createKinoFooter(LinearLayout rootView) {
        View view;
        TextView text;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // TODO: replace those copypasted blocks with a function
        // genre
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = view.findViewById(R.id.list_header);
        text.setText(R.string.genre);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_GENRE)));
        rootView.addView(view);

        // director
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = view.findViewById(R.id.list_header);
        text.setText(R.string.director);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_DIRECTOR)));
        rootView.addView(view);

        // actors
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = view.findViewById(R.id.list_header);
        text.setText(R.string.actors);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_ACTORS)));
        rootView.addView(view);

        // description
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = view.findViewById(R.id.list_header);
        text.setText(R.string.description);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_DESCRIPTION)));
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

    private void createKinoHeader(LinearLayout rootView) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        date.setText(KinoDetailsFragment.formDateString(cursor.getString(cursor.getColumnIndex(Const.JSON_DATE))));
        link.setText(R.string.www);
        imdb.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_RATING)));
        year.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_YEAR)));
        runtime.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_RUNTIME)));

        // set onClickListener
        date.setOnClickListener(this);
        link.setOnClickListener(this);
        imdb.setOnClickListener(this);
        year.setOnClickListener(this);
        runtime.setOnClickListener(this);

        // cover
        String coverUrl = cursor.getString(cursor.getColumnIndex(Const.JSON_COVER));
        net.loadAndSetImage(coverUrl, cover);

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

}
