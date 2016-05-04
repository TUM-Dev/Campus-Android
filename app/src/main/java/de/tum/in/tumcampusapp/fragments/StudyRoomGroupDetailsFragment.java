package de.tum.in.tumcampusapp.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.StudyRoom;
import de.tum.in.tumcampusapp.models.managers.StudyRoomGroupManager;

/**
 * Fragment for each study room group. Shows study room details in a list.
 */
public class StudyRoomGroupDetailsFragment extends Fragment implements SimpleCursorAdapter
        .ViewBinder {
    private int mStudyRoomGroupId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(Const.STUDY_ROOM_GROUP_ID)) {
            mStudyRoomGroupId = getArguments().getInt(Const.STUDY_ROOM_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        Cursor studyRoomCursor = new StudyRoomGroupManager(getActivity()).getStudyRoomsFromDb
                (mStudyRoomGroupId);
        SimpleCursorAdapter adapter = createStudyRoomCursorAdapter(studyRoomCursor);
        adapter.setViewBinder(this);

        ListView lv2 = (ListView) rootView.findViewById(R.id.fragment_item_detail_listview);
        lv2.setDividerHeight(0);
        lv2.setAdapter(adapter);

        return rootView;
    }

    @NonNull
    private SimpleCursorAdapter createStudyRoomCursorAdapter(final Cursor studyRoomCursor) {
        return new SimpleCursorAdapter(getActivity(),
                R.layout.study_room_list_item, studyRoomCursor, studyRoomCursor.getColumnNames(),
                new int[]{android.R.id.text1, android.R.id.text2, R.id.text3}, 0) {

            @Override
            public boolean isEnabled(int position) {
                // disable onclick
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                View cardView = view.findViewById(R.id.card_view);
                TextView text = (TextView) view.findViewById(android.R.id.text2);

                LayerDrawable bgDrawable = (LayerDrawable) cardView.getBackground();
                GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id
                        .study_room_card_body);
                if (text.getText().toString().contains(getString(R.string.free))) {
                    shape.setColor(Color.rgb(200, 230, 201));
                } else {
                    shape.setColor(Color.rgb(255, 205, 210));
                }

                return view;
            }
        };
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        StudyRoom studyRoom = StudyRoomGroupManager.getStudyRoomFromCursor(cursor);

        if (view.getId() == android.R.id.text1) {
            TextView tv = (TextView) view;
            tv.setText(studyRoom.name);
        } else if (view.getId() == android.R.id.text2) {
            StringBuilder stringBuilder = new StringBuilder(studyRoom.location + "<br>");

            if (studyRoom.occupiedTill.compareTo(new Date()) < 0) {
                stringBuilder.append(getString(R.string.free));
            } else {
                stringBuilder.append(getString(R.string.occupied))
                        .append(" <b>")
                        .append(new SimpleDateFormat("HH:mm").format(studyRoom.occupiedTill))
                        .append("</b>");
            }

            TextView tv = (TextView) view;
            tv.setText(Html.fromHtml(stringBuilder.toString()));
        } else if (view.getId() == R.id.text3) {
            TextView tv = (TextView) view;
            tv.setText(studyRoom.code);
        }
        return true;
    }
}
