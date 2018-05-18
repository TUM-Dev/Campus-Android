package de.tum.in.tumcampusapp.component.ui.studyroom;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoom;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Fragment for each study room group. Shows study room details in a list.
 */
public class StudyRoomGroupDetailsFragment extends Fragment {
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
        StudyRoomGroupManager manager = new StudyRoomGroupManager(getActivity());

        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_item_detail_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new StudyRoomAdapter(manager.getAllStudyRoomsForGroup(mStudyRoomGroupId)));

        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        return rootView;
    }

    private class StudyRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<StudyRoom> studyRooms;

        StudyRoomAdapter(List<StudyRoom> studyRooms) {
            this.studyRooms = studyRooms;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.two_line_list_item, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            StudyRoom room = studyRooms.get(position);
            CardView cardView = holder.itemView.findViewById(R.id.card_view);

            TextView headerTextView = holder.itemView.findViewById(R.id.headerTextView);
            TextView detailsTextView = holder.itemView.findViewById(R.id.detailsTextView);

            //TextView locationTextView = holder.itemView.findViewById(R.id.text3);
            AppCompatButton openLinkButton = holder.itemView.findViewById(R.id.openLinkButton);
            openLinkButton.setText(R.string.go_to_room);
            openLinkButton.setTag(room.getCode());

            headerTextView.setText(room.getName());

            StringBuilder stringBuilder = new StringBuilder(room.getLocation()).append("<br>");

            if (room.getOccupiedTill().compareTo(new Date()) < 0) {
                stringBuilder.append(getString(R.string.free));
            } else {
                stringBuilder.append(getString(R.string.occupied))
                             .append(" <b>")
                             .append(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(room.getOccupiedTill()))
                             .append("</b>");
            }

            detailsTextView.setText(Utils.fromHtml(stringBuilder.toString()));

            int color;
            if (detailsTextView.getText()
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
            return studyRooms.size();
        }

    }
}
