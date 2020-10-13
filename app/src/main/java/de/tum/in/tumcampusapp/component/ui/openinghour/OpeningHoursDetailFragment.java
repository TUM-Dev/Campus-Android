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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;
import de.tum.in.tumcampusapp.database.TcaDb;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link OpeningHoursListActivity} in two-pane mode (on tablets)
 * or a {@link OpeningHoursDetailActivity} on handsets.
 * <p/>
 * NEEDS: ARG_ITEM_ID and ARG_ITEM_CONTENT set in arguments
 */
public class OpeningHoursDetailFragment extends Fragment {
    static final String ARG_ITEM_ID = "item_id";
    static final String ARG_ITEM_CONTENT = "item_content";
    static final String TWO_PANE = "two_pane";
    private static final Pattern COMPILE = Pattern.compile("\\\\n");

    private int mItemId;
    private String mItemContent;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OpeningHoursDetailFragment() {
        // NOP
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getInt(ARG_ITEM_ID);
            mItemContent = getArguments().getString(ARG_ITEM_CONTENT);
        }
        if (getArguments().containsKey(TWO_PANE) && !getArguments().getBoolean(TWO_PANE)) {
            getActivity().setTitle(mItemContent);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // click on category in list
        LocationDao dao = TcaDb.Companion.getInstance(getActivity())
                                         .locationDao();
        String[] categories = {"library", "info", "cafeteria_gar", "cafeteria_grh", "cafeteria", "cafeteria_pas", "cafeteria_wst"};
        List<Location> locations = dao.getAllOfCategory(categories[mItemId]);

        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_item_detail_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new OpeningHoursDetailAdapter(locations));

        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        return rootView;
    }

    /**
     * change presentation of locations in the list
     */
    public static void bindLocationToView(View view, Location location) {
        TextView locationTextView = view.findViewById(R.id.headerTextView);
        locationTextView.setText(location.getName());

        String transport = location.getTransport();
        String address = location.getAddress();
        String hours = location.getHours();
        String remark = location.getRemark();
        String room = location.getRoom();

        MaterialButton openLinkButton = view.findViewById(R.id.openLinkButton);
        if (location.getUrl()
                    .isEmpty()) {
            openLinkButton.setVisibility(View.GONE);
        } else {
            openLinkButton.setVisibility(View.VISIBLE);
            openLinkButton.setText(R.string.website);
            openLinkButton.setTag(location.getUrl());
        }

        TextView hoursView = view.findViewById(R.id.opening_hours_hours);
        hoursView.setVisibility(hours.isEmpty() ? View.GONE : View.VISIBLE);
        hoursView.setText(hours.replace("/n", "\n"));

        TextView roomView = view.findViewById(R.id.opening_hours_room);
        roomView.setVisibility(room.isEmpty() ? View.GONE : View.VISIBLE);
        roomView.setText(room);

        TextView locationView = view.findViewById(R.id.opening_hours_location);
        locationView.setVisibility(address.isEmpty() ? View.GONE : View.VISIBLE);
        locationView.setText(address.replace("/n", "\n"));

        TextView transportView = view.findViewById(R.id.opening_hours_transport);
        transportView.setVisibility(transport.isEmpty() ? View.GONE : View.VISIBLE);
        transportView.setText(transport.replace("/n", "\n"));

        TextView infoView = view.findViewById(R.id.opening_hours_info);
        infoView.setVisibility(remark.isEmpty() ? View.GONE : View.VISIBLE);
        String infoText = remark.replace("/n", "\n")
                                .replace("\\n", "\n");
        infoView.setText(infoText);

        // link email addresses and phone numbers (e.g. 089-123456)
        Linkify.addLinks(infoView, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        Linkify.addLinks(infoView, Pattern.compile("[0-9-]{6,}"), "tel:");
    }

    private class OpeningHoursDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final List<Location> locations;

        OpeningHoursDetailAdapter(List<Location> locations) {
            this.locations = locations;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                                   .inflate(R.layout.card_opening_hour_details, parent, false);
            return new RecyclerView.ViewHolder(v) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            OpeningHoursDetailFragment.bindLocationToView(holder.itemView, locations.get(position));
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }

    }

}
