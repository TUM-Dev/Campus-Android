package de.tum.in.tumcampusapp.component.ui.openinghour;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Location;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link OpeningHoursListActivity} in two-pane mode (on tablets)
 * or a {@link OpeningHoursDetailActivity} on handsets.
 * <p/>
 * NEEDS: ARG_ITEM_ID and ARG_ITEM_CONTENT set in arguments
 */
public class OpeningHoursDetailFragment extends Fragment {

    private static final Pattern COMPILE = Pattern.compile("\\\\n");
    private static final String[] categories = {
            "library", "info", "cafeteria_gar",
            "cafeteria_grh", "cafeteria", "cafeteria_pas", "cafeteria_wst"
    };

    static final String ARG_ITEM_ID = "item_id";
    static final String ARG_ITEM_CONTENT = "item_content";
    static final String TWO_PANE = "two_pane";

    private int mItemId;
    private String mItemContent;

    @Inject
    CafeteriaLocalRepository localRepository;

    public static OpeningHoursDetailFragment newInstance(int itemId,
                                                         String content, boolean isTwoPane) {
        OpeningHoursDetailFragment fragment = new OpeningHoursDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(OpeningHoursDetailFragment.ARG_ITEM_ID, itemId);
        arguments.putString(OpeningHoursDetailFragment.ARG_ITEM_CONTENT, content);
        arguments.putBoolean(OpeningHoursDetailFragment.TWO_PANE, isTwoPane);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) requireActivity()).getInjector().inject(this);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getInt(ARG_ITEM_ID);
            mItemContent = getArguments().getString(ARG_ITEM_CONTENT);
        }

        if (getArguments() != null
                && getArguments().containsKey(TWO_PANE) && !getArguments().getBoolean(TWO_PANE)) {
            requireActivity().setTitle(mItemContent);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        List<Location> locations = localRepository.getAllLocationsOfCategory(categories[mItemId]);

        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_item_detail_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new OpeningHoursDetailAdapter(locations));

        final int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        return rootView;
    }

    /**
     * change presentation of locations in the list
     */
    public void setViewValue(View view, Location location) {
        TextView locationTextView = view.findViewById(R.id.headerTextView);
        locationTextView.setText(location.getName());

        TextView detailsTextView = view.findViewById(R.id.detailsTextView);
        String transport = location.getTransport();
        String address = location.getAddress();
        String hours = location.getHours();
        String remark = location.getRemark();
        String room = location.getRoom();

        MaterialButton openLinkButton = view.findViewById(R.id.openLinkButton);

        if (location.getUrl().isEmpty()) {
            openLinkButton.setVisibility(View.GONE);
        } else {
            openLinkButton.setVisibility(View.VISIBLE);
            openLinkButton.setText(R.string.website);
            openLinkButton.setTag(location.getUrl());
        }

        StringBuilder sb = new StringBuilder(hours).append('\n')
                                                   .append(address);
        if (!room.isEmpty()) {
            sb.append(", ")
              .append(room);
        }
        if (!transport.isEmpty()) {
            sb.append(" (")
              .append(transport)
              .append(')');
        }
        if (!remark.isEmpty()) {
            sb.append('\n')
              .append(COMPILE.matcher(remark)
                             .replaceAll("\n"));
        }
        detailsTextView.setText(sb.toString());

        // link email addresses and phone numbers (e.g. 089-123456)
        Linkify.addLinks(detailsTextView, Linkify.EMAIL_ADDRESSES);
        Linkify.addLinks(detailsTextView, Pattern.compile("[0-9-]{6,}"), "tel:");
    }

    private class OpeningHoursDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final List<Location> locations;

        OpeningHoursDetailAdapter(List<Location> locations) {
            this.locations = locations;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_header_details_button, parent, false);
            return new RecyclerView.ViewHolder(v) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            OpeningHoursDetailFragment.this.setViewValue(holder.itemView, locations.get(position));
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }

    }

}
