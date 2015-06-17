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

        ImageView cover = new ImageView(context);
        TextView description = new TextView(context);
        description.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);
        description.setLineSpacing(0, 1.2f);

        cursor.moveToPosition(position);


        net.loadAndSetImage(cursor.getString(cursor.getColumnIndex(Const.JSON_COVER)), cover);
        description.setText(cursor.getString(cursor.getColumnIndex(Const.JSON_DESCRIPTION)));


        // add views to the rootView
        rootView.addView(cover);
        rootView.addView(description);


        cursor.close();

    }

}
