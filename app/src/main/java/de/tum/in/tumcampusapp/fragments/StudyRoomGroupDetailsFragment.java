package de.tum.in.tumcampusapp.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.StudyRoomGroupManager;
import de.tum.in.tumcampusapp.models.tumcabe.StudyRoom;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;
        try (Cursor studyRoomCursor = new StudyRoomGroupManager(getActivity()).getStudyRoomsFromDb(mStudyRoomGroupId)) {
            recyclerView = rootView.findViewById(R.id.fragment_item_detail_recyclerview);
            recyclerView.setAdapter(new StudyRoomAdapter(studyRoomCursor));
        }
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    private class StudyRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final SimpleCursorAdapter mCursorAdapter;

        StudyRoomAdapter(Cursor studyRoomCursor) {
            mCursorAdapter = createStudyRoomCursorAdapter(studyRoomCursor);
            mCursorAdapter.setViewBinder(StudyRoomGroupDetailsFragment.this);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mCursorAdapter.newView(getContext(), mCursorAdapter.getCursor(), parent);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            mCursorAdapter.getCursor()
                          .moveToPosition(position);
            mCursorAdapter.bindView(holder.itemView, getContext(), mCursorAdapter.getCursor());

            CardView cardView = holder.itemView.findViewById(R.id.card_view);
            TextView text = holder.itemView.findViewById(android.R.id.text2);

            int color;
            if (text.getText()
                    .toString()
                    .contains(getString(R.string.free))) {
                color = Color.rgb(200, 230, 201);
            } else {
                color = Color.rgb(255, 205, 210);
            }

            cardView.setCardBackgroundColor(color);
        }

        @Override
        public int getItemCount() {
            return mCursorAdapter.getCount();
        }

    }

    @NonNull
    private SimpleCursorAdapter createStudyRoomCursorAdapter(final Cursor studyRoomCursor) {
        return new SimpleCursorAdapter(getActivity(),
                                       R.layout.two_line_list_item, studyRoomCursor, studyRoomCursor.getColumnNames(),
                                       new int[]{android.R.id.text1, android.R.id.text2, R.id.text3}, 0) {

            @Override
            public boolean isEnabled(int position) {
                // disable onclick
                return false;
            }
        };
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        StudyRoom studyRoom = StudyRoomGroupManager.getStudyRoomFromCursor(cursor);

        if (view.getId() == android.R.id.text1) {
            TextView tv = (TextView) view;
            tv.setText(studyRoom.getName());
        } else if (view.getId() == android.R.id.text2) {
            StringBuilder stringBuilder = new StringBuilder(studyRoom.getLocation()).append("<br>");

            if (studyRoom.getOccupiedTill()
                         .compareTo(new Date()) < 0) {
                stringBuilder.append(getString(R.string.free));
            } else {
                stringBuilder.append(getString(R.string.occupied))
                             .append(" <b>")
                             .append(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(studyRoom.getOccupiedTill()))
                             .append("</b>");
            }

            TextView tv = (TextView) view;
            tv.setText(Utils.fromHtml(stringBuilder.toString()));
        } else if (view.getId() == R.id.text3) {
            TextView tv = (TextView) view;
            tv.setText(studyRoom.getCode());
        }
        return true;
    }
}
