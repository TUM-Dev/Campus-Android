package de.tum.in.tumcampusapp.component.ui.studyroom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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

        if (getArguments() != null && getArguments().containsKey(Const.STUDY_ROOM_GROUP_ID)) {
            mStudyRoomGroupId = getArguments().getInt(Const.STUDY_ROOM_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        StudyRoomGroupManager manager = new StudyRoomGroupManager(requireContext());

        List<StudyRoom> studyRooms = manager.getAllStudyRoomsForGroup(mStudyRoomGroupId);
        StudyRoomAdapter adapter = new StudyRoomAdapter(studyRooms);

        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_item_detail_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        return rootView;
    }

    private class StudyRoomAdapter extends RecyclerView.Adapter<StudyRoomViewHolder> {

        private List<StudyRoom> studyRooms;

        StudyRoomAdapter(List<StudyRoom> studyRooms) {
            this.studyRooms = studyRooms;
        }

        @NonNull
        @Override
        public StudyRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.two_line_list_item, parent, false);
            return new StudyRoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudyRoomViewHolder holder, int position) {
            StudyRoom room = studyRooms.get(position);

            holder.openRoomFinderButton.setText(R.string.go_to_room);
            holder.openRoomFinderButton.setTag(room.getCode());

            holder.headerTextView.setText(room.getName());

            StringBuilder stringBuilder = new StringBuilder(room.getBuildingName()).append("<br>");

            DateTime occupiedUntil = room.getOccupiedUntil();
            boolean isFree = occupiedUntil == null || occupiedUntil.isBeforeNow();

            if (isFree) {
                stringBuilder.append(getString(R.string.free));
            } else {
                stringBuilder.append(getString(R.string.occupied))
                             .append(" <b>")
                             .append(DateTimeFormat.forPattern("HH:mm")
                                                   .withLocale(Locale.getDefault())
                                                   .print(room.getOccupiedUntil()))
                             .append("</b>");
            }

            holder.detailsTextView.setText(Utils.fromHtml(stringBuilder.toString()));

            int colorResId = isFree ? R.color.study_room_free : R.color.study_room_occupied;
            int color = ContextCompat.getColor(holder.itemView.getContext(), colorResId);
            holder.cardView.setCardBackgroundColor(color);
        }

        @Override
        public int getItemCount() {
            return studyRooms.size();
        }

    }

    private static class StudyRoomViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView headerTextView;
        TextView detailsTextView;
        MaterialButton openRoomFinderButton;

        StudyRoomViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            headerTextView = itemView.findViewById(R.id.headerTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
            openRoomFinderButton = itemView.findViewById(R.id.openLinkButton);
        }

    }

}
