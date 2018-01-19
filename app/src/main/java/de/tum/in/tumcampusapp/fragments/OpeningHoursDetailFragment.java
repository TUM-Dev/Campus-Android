package de.tum.in.tumcampusapp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.OpeningHoursDetailActivity;
import de.tum.in.tumcampusapp.activities.OpeningHoursListActivity;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dao.LocationDao;
import de.tum.in.tumcampusapp.models.cafeteria.Location;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link OpeningHoursListActivity} in two-pane mode (on tablets)
 * or a {@link OpeningHoursDetailActivity} on handsets.
 * <p/>
 * NEEDS: ARG_ITEM_ID and ARG_ITEM_CONTENT set in arguments
 */
public class OpeningHoursDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_CONTENT = "item_content";
    public static final String TWO_PANE = "two_pane";
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
        LocationDao dao = TcaDb.getInstance(getActivity())
                               .locationDao();
        String[] categories = {"library", "info", "cafeteria_gar", "cafeteria_grh", "cafeteria", "cafeteria_pas", "cafeteria_wst"};
        List<Location> locations = dao.getAllOfCategory(categories[mItemId]);
        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_item_detail_recyclerview);
        recyclerView.setAdapter(new OpeningHoursDetailAdapter(locations));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    /**
     * change presentation of locations in the list
     */
    public void setViewValue(View view, Location location) {
        TextView tv1 = view.findViewById(android.R.id.text1);
        tv1.setText(location.getName());

        TextView tv2 = view.findViewById(android.R.id.text2);
        String transport = location.getTransport();
        String address = location.getAddress();
        String hours = location.getHours();
        String remark = location.getRemark();
        String room = location.getRoom();

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
        tv2.setText(sb.toString());

        // link email addresses and phone numbers (e.g. 089-123456)
        Linkify.addLinks(tv2, Linkify.EMAIL_ADDRESSES);
        Linkify.addLinks(tv2, Pattern.compile("[0-9-]{6,}"), "tel:");

        StringBuilder url = new StringBuilder(location.getUrl());
        TextView tv3 = view.findViewById(R.id.text3);
        if (url.toString()
               .isEmpty()) {
            tv3.setVisibility(View.GONE);
        } else {
            url.insert(0, "<a href=\"")
               .append("\">")
               .append(getString(R.string.website))
               .append("</a>");
            tv3.setMovementMethod(LinkMovementMethod.getInstance());
            tv3.setText(Utils.fromHtml(url.toString()));
        }
    }

    private class OpeningHoursDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final List<Location> locations;

        OpeningHoursDetailAdapter(List<Location> locations) {
            this.locations = locations;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                                   .inflate(R.layout.two_line_list_item, parent);
            return new RecyclerView.ViewHolder(v) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            OpeningHoursDetailFragment.this.setViewValue(holder.itemView, locations.get(position));
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }
    }
}
