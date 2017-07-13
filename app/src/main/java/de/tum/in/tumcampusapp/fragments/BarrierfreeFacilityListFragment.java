package de.tum.in.tumcampusapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.tumonline.TUMBarrierFreeRequest;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequestFetchListener;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierfreeFacilityListFragment extends Fragment implements TUMRoomFinderRequestFetchListener{
    private TUMBarrierFreeRequest request;
    private StickyListHeadersListView stickyList;
    private RoomFinderListAdapter adapter;
    private int pageID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barrierfree_facility_list, container, false);
        stickyList = (StickyListHeadersListView) view.findViewById(R.id.activity_barrier_free_facilities_list_view);
        request = new TUMBarrierFreeRequest(getActivity());

        if (getArguments().containsKey(Const.BARRIER_FREE_FACILITY_PAGE_ID)) {
            pageID = getArguments().getInt(Const.BARRIER_FREE_FACILITY_PAGE_ID);
        }

        switch (pageID){
            case 0:
                //// TODO: 7/13/2017 nerby facilities
                break;
            case 1:
                System.out.println("Toilet");
                request.fetchListOfToilets(getActivity(), this);
                break;
            case 2:
                request.fetchListOfElevators(getActivity(),this);
                break;
            default:
                break;
        }
        return view;
    }

    @Override
    public void onFetch(List<Map<String, String>> result) {
        System.out.println(result.size());
        adapter = new RoomFinderListAdapter(getActivity(), result);
        stickyList.setAdapter(adapter);
    }

    @Override
    public void onFetchError(String errorReason) {
        request.cancelRequest(true);
    }

    @Override
    public void onNoInternetError() {
    }
}
