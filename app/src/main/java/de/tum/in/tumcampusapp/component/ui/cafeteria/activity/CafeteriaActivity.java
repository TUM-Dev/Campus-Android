package de.tum.in.tumcampusapp.component.ui.cafeteria.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuFormatter;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionsPagerAdapter;
import de.tum.in.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel;
import de.tum.in.tumcampusapp.component.ui.cafeteria.di.CafeteriaModule;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import de.tum.in.tumcampusapp.di.ViewModelFactory;
import de.tum.in.tumcampusapp.service.DownloadWorker;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Lists all dishes at selected cafeteria
 * <p>
 * OPTIONAL: Const.CAFETERIA_ID set in incoming bundle (cafeteria to show)
 */
public class CafeteriaActivity extends ActivityForDownloadingExternal
        implements AdapterView.OnItemSelectedListener {

    private static final int NONE_SELECTED = -1;

    private List<Cafeteria> mCafeterias = new ArrayList<>();

    private CafeteriaViewModel cafeteriaViewModel;

    private ArrayAdapter<Cafeteria> adapter;
    private CafeteriaDetailsSectionsPagerAdapter sectionsPagerAdapter;

    private ViewPager viewPager;
    private Spinner spinner;

    @Inject
    Provider<CafeteriaViewModel> viewModelProvider;

    @Inject
    LocationManager locationManager;

    @Inject
    CafeteriaManager cafeteriaManager;

    @Inject
    DownloadWorker.Action cafeteriaDownloadAction;

    public CafeteriaActivity() {
        super(R.layout.activity_cafeteria);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().cafeteriaComponent()
                .cafeteriaModule(new CafeteriaModule())
                .build()
                .inject(this);

        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(50);

        adapter = createArrayAdapter();

        spinner = findViewById(R.id.spinnerToolbar);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        sectionsPagerAdapter = new CafeteriaDetailsSectionsPagerAdapter(getSupportFragmentManager());

        ViewModelFactory<CafeteriaViewModel> factory = new ViewModelFactory<>(viewModelProvider);
        cafeteriaViewModel = ViewModelProviders.of(this, factory).get(CafeteriaViewModel.class);

        cafeteriaViewModel.getCafeterias().observe(this, this::updateCafeteria);
        cafeteriaViewModel.getSelectedCafeteria().observe(this, this::onNewCafeteriaSelected);
        cafeteriaViewModel.getMenuDates().observe(this, this::updateSectionsPagerAdapter);

        cafeteriaViewModel.getError().observe(this, isError -> {
            if (isError) {
                showError(R.string.error_something_wrong);
            } else {
                showContentLayout();
            }
        });
    }

    @Nullable
    @Override
    public DownloadWorker.Action getMethod() {
        return cafeteriaDownloadAction;
    }

    private void updateCafeteria(List<Cafeteria> cafeterias) {
        mCafeterias = cafeterias;
        adapter.clear();
        adapter.addAll(cafeterias);
        adapter.notifyDataSetChanged();
        initCafeteriaSpinner();
    }

    private ArrayAdapter<Cafeteria> createArrayAdapter() {
        return new ArrayAdapter<Cafeteria>(
                this, R.layout.simple_spinner_item_actionbar) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = inflater.inflate(
                        R.layout.simple_spinner_dropdown_item_actionbar_two_line, parent, false);
                Cafeteria cafeteria = getItem(position);

                TextView name = v.findViewById(android.R.id.text1);
                TextView address = v.findViewById(android.R.id.text2);
                TextView distance = v.findViewById(R.id.distance);

                if (cafeteria != null) {
                    name.setText(cafeteria.getName());
                    address.setText(cafeteria.getAddress());
                    distance.setText(Utils.formatDistance(cafeteria.getDistance()));
                }

                return v;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Location location = locationManager.getCurrentOrNextLocation();
        cafeteriaViewModel.fetchCafeterias(location);
    }

    private void initCafeteriaSpinner() {
        final Intent intent = getIntent();
        int cafeteriaId;

        if (intent != null && intent.hasExtra(Const.MENSA_FOR_FAVORITEDISH)) {
            cafeteriaId = intent.getIntExtra(Const.MENSA_FOR_FAVORITEDISH, NONE_SELECTED);
            intent.removeExtra(Const.MENSA_FOR_FAVORITEDISH);
        } else if (intent != null && intent.hasExtra(Const.CAFETERIA_ID)) {
            cafeteriaId = intent.getIntExtra(Const.CAFETERIA_ID, 0);
        } else {
            // If we're not provided with a cafeteria ID, we choose the best matching cafeteria.
            cafeteriaId = cafeteriaManager.getBestMatchMensaId();
        }

        updateCafeteriaSpinner(cafeteriaId);
    }

    private void onNewCafeteriaSelected(Cafeteria cafeteria) {
        sectionsPagerAdapter.setCafeteriaId(cafeteria.getId());
        cafeteriaViewModel.fetchMenuDates();
    }

    private void updateCafeteriaSpinner(int cafeteriaId) {
        int selectedIndex = NONE_SELECTED;

        for (int i = 0; i < mCafeterias.size(); i++) {
            Cafeteria cafeteria = mCafeterias.get(i);
            if (cafeteriaId == NONE_SELECTED || cafeteriaId == cafeteria.getId()) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex != NONE_SELECTED) {
            spinner.setSelection(selectedIndex);
        }
    }

    /**
     * Switch cafeteria if a new cafeteria has been selected
     *
     * @param parent the parent view
     * @param pos    index of the new selection
     * @param id     id of the selected item
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Cafeteria selected = mCafeterias.get(pos);
        cafeteriaViewModel.updateSelectedCafeteria(selected);
    }

    private void updateSectionsPagerAdapter(List<DateTime> menuDates) {
        viewPager.setAdapter(null);
        sectionsPagerAdapter.update(menuDates);
        viewPager.setAdapter(sectionsPagerAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Don't change anything
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_section_fragment_cafeteria_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ingredients:
                showIngredientsInfo();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, CafeteriaNotificationSettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showIngredientsInfo() {
        // Build a alert dialog containing the mapping of ingredients to the numbers
        CafeteriaMenuFormatter formatter = new CafeteriaMenuFormatter(this);
        SpannableString message = formatter.format(R.string.cafeteria_ingredients, true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_ingredients)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        }

        dialog.show();
    }

}
