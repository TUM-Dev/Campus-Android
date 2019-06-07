package de.tum.in.tumcampusapp.component.ui.openinghour;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.fragment.app.ListFragment;

import org.jetbrains.annotations.NotNull;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.navigation.NavigationDestination;
import de.tum.in.tumcampusapp.component.other.navigation.NavigationManager;
import de.tum.in.tumcampusapp.component.other.navigation.SystemActivity;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link OpeningHoursDetailFragment}.
 */
public class OpeningHoursListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = AdapterView.INVALID_POSITION;

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

        setListAdapter(new ArrayAdapter<>(requireContext(), layout, android.R.id.text1, names));
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        final ListAdapter adapter = getListAdapter();
        onItemSelected(position, adapter.getItem(position).toString());
    }

    private void onItemSelected(int id, String name) {
        Bundle options = new Bundle();
        options.putInt(OpeningHoursDetailFragment.ARG_ITEM_ID, id);
        options.putString(OpeningHoursDetailFragment.ARG_ITEM_CONTENT, name);

        NavigationDestination destination =
                new SystemActivity(OpeningHoursDetailActivity.class, options);
        NavigationManager.open(requireContext(), destination);
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

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    private void setActivatedPosition(int position) {
        if (position == AdapterView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

}
