package de.tum.in.tumcampus.fragments;

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

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.models.managers.KinoManager;


/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment implements View.OnClickListener{

    private Context context;
    private Cursor cursor;
    private NetUtils net;
    private String url; // link to homepage

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = (LinearLayout) rootView.findViewById(R.id.layout);

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
     * @param rootView view on which the content gets drawn
     * @param position position in database
     */
    private void showDetails(LinearLayout rootView, int position){
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

        // genre
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText(R.string.genre);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_GENRE)));
        rootView.addView(view);

        // director
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText(R.string.director);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_DIRECTOR)));
        rootView.addView(view);

        // actors
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText(R.string.actors);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_ACTORS)));
        rootView.addView(view);

        // description
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText(R.string.description);
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_DESCRIPTION)));
        // padding is done programmatically here because we need more padding at the end
        int padding = (int) context.getResources().getDimension(R.dimen.padding_kino);
        int padding_right = (int) context.getResources().getDimension(R.dimen.padding_kino_right);
        int padding_end = (int) context.getResources().getDimension(R.dimen.padding_kino_end);
        text.setPadding(padding,padding,padding_right,padding_end);
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
        ImageView cover = (ImageView) headerView.findViewById(R.id.kino_cover);

        // initialize all buttons
        date = (Button) headerView.findViewById(R.id.button_date);
        link = (Button) headerView.findViewById(R.id.button_link);
        imdb = (Button) headerView.findViewById(R.id.button_imdb);
        year = (Button) headerView.findViewById(R.id.button_year);
        runtime = (Button) headerView.findViewById(R.id.button_runtime);

        // set text for all buttons
        date.setText(this.formDateString(cursor.getString(cursor.getColumnIndex(Const.JSON_DATE))));
        link.setText("www");
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
        String cover_url = cursor.getString(cursor.getColumnIndex(Const.JSON_COVER));
        net.loadAndSetImage(cover_url, cover);

        rootView.addView(headerView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_date:
                Toast.makeText(context, R.string.date, Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_link:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;
            case R.id.button_imdb:
                Toast.makeText(context, R.string.imdb_rating, Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_year:
                Toast.makeText(context, R.string.year, Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_runtime:
                Toast.makeText(context, R.string.runtime, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * formats the dateString
     * @param date Date string stored in database
     * @return formated dateString
     */
    private String formDateString(String date){
        String new_date;
        new_date = date.substring(8, 10) + "." + date.substring(5,7) + ".";
        return new_date;
    }

}
