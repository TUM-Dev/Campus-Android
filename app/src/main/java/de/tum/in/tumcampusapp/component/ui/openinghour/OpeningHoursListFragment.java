package de.tum.in.tumcampusapp.component.ui.openinghour;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.fragment.BaseFragment;
import kotlin.Unit;

public class OpeningHoursListFragment extends BaseFragment<Unit>
        implements AdapterView.OnItemClickListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = AdapterView.INVALID_POSITION;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    public OpeningHoursListFragment() {
        super(R.layout.fragment_opening_hours_list, R.string.opening_hours);
    }

    public static OpeningHoursListFragment newInstance() {
        return new OpeningHoursListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layout = android.R.layout.simple_list_item_activated_1;

        String[] names = {getString(R.string.libraries),
                          getString(R.string.information),
                          getString(R.string.mensa_garching),
                          getString(R.string.mensa_großhadern),
                          getString(R.string.mensa_city),
                          getString(R.string.mensa_pasing),
                          getString(R.string.mensa_weihenstephan)};
        adapter = new ArrayAdapter<>(requireContext(), layout, android.R.id.text1, names);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String item = adapter.getItem(position);
        onItemSelected(position, item);
    }

    private void onItemSelected(int id, String name) {
        Bundle args = new Bundle();
        args.putInt(OpeningHoursDetailFragment.ARG_ITEM_ID, id);
        args.putString(OpeningHoursDetailFragment.ARG_ITEM_CONTENT, name);

        Intent intent = new Intent(requireContext(), OpeningHoursDetailActivity.class);
        intent.putExtras(args);
        requireContext().startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != AdapterView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = requireActivity().findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    private void setActivatedPosition(int position) {
        if (position == AdapterView.INVALID_POSITION) {
            listView.setItemChecked(mActivatedPosition, false);
        } else {
            listView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

}
