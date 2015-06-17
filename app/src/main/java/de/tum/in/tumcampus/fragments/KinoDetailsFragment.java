package de.tum.in.tumcampus.fragments;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.models.managers.KinoManager;


/**
 * Fragment for KinoDetails. Manages content that gets shown on the pagerView
 */
public class KinoDetailsFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_kinodetails_section, container, false);
        LinearLayout root = (LinearLayout) rootView.findViewById(R.id.layout);
        int position = getArguments().getInt(Const.POSITION);

        showDetails(root, position);

        return rootView;
    }

    /**
     * creates the content of the fragment
     * @param rootView view on which the content gets drawn
     * @param position position in database
     */
    public static void showDetails(LinearLayout rootView, int position){

        // often used variables
        final Context context = rootView.getContext();
        final Cursor cursor = new KinoManager(context).getAllFromDb();
        final NetUtils net = new NetUtils(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        TextView text;

        cursor.moveToPosition(position);

        // cover
        ImageView cover = new ImageView(context);
        net.loadAndSetImage(cursor.getString(cursor.getColumnIndex(Const.JSON_COVER)), cover);
        rootView.addView(cover);

        // genre
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText("Genre");
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_GENRE)));
        rootView.addView(view);

        // director
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText("Director");
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_DIRECTOR)));
        rootView.addView(view);

        // actors
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText("Actors");
        rootView.addView(view);
        view = inflater.inflate(R.layout.kino_content, rootView, false);
        text = (TextView) view.findViewById(R.id.line_name);
        text.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_ACTORS)));
        rootView.addView(view);

        // description
        view = inflater.inflate(R.layout.list_header_big, rootView, false);
        text = (TextView) view.findViewById(R.id.list_header);
        text.setText("Description");
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


        cursor.close();

    }

}
