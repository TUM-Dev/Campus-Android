package de.tum.in.tumcampusapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.adapters.CafeteriaDetailsSectionsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.repository.LocalRepositoryImpl;
import de.tum.in.tumcampusapp.database.repository.RemoteRepositoryImpl;
import de.tum.in.tumcampusapp.database.viewmodel.CafeteriaViewModel;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.models.cafeteria.Cafeteria;
import io.reactivex.disposables.CompositeDisposable;

import static de.tum.in.tumcampusapp.fragments.CafeteriaDetailsSectionFragment.menuToSpan;

/**
 * Lists all dishes at selected cafeteria
 * <p>
 * OPTIONAL: Const.CAFETERIA_ID set in incoming bundle (cafeteria to show)
 */
public class CafeteriaActivity extends ActivityForDownloadingExternal implements AdapterView.OnItemSelectedListener {

    private ViewPager mViewPager;
    private int mCafeteriaId = -1;
    private CafeteriaViewModel cafeteriaViewModel;
    private List<Cafeteria> mCafeterias = new LinkedList<>();

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public CafeteriaActivity() {
        super(Const.CAFETERIAS, R.layout.activity_cafeteria);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get id from intent if specified
        final Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras()
                                                                  .containsKey(Const.CAFETERIA_ID)) {
            mCafeteriaId = intent.getExtras()
                                 .getInt(Const.CAFETERIA_ID);
        }

        mViewPager = findViewById(R.id.pager);

        /*
         *set pagelimit to avoid losing toggle button state.
         *by default it's 1.
         */
        mViewPager.setOffscreenPageLimit(50);
        cafeteriaViewModel = new CafeteriaViewModel(LocalRepositoryImpl.getInstance(this), RemoteRepositoryImpl.getInstance(this), mDisposable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add info icon to show ingredients
        getMenuInflater().inflate(R.menu.menu_section_fragment_cafeteria_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ingredients) {
            // Build a alert dialog containing the mapping of ingredients to the numbers
            new AlertDialog.Builder(this).setTitle(R.string.action_ingredients)
                                         .setMessage(menuToSpan(this, getResources().getString(R.string.cafeteria_ingredients)))
                                         .setPositiveButton(android.R.string.ok, null)
                                         .create()
                                         .show();
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, CafeteriaNotificationSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup action bar navigation (to switch between cafeterias)
     */
    @Override
    protected void onStart() {
        super.onStart();
//        cafeteriaViewModel.getCafeteriasFromService();



        // Adapter for drop-down navigation
        ArrayAdapter adapterCafeterias = new ArrayAdapter<Cafeteria>(this, R.layout.simple_spinner_item_actionbar, android.R.id.text1, mCafeterias) {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = inflater.inflate(R.layout.simple_spinner_dropdown_item_actionbar, parent, false);
                Cafeteria c = getItem(position);

                TextView name = v.findViewById(android.R.id.text1); // Set name
                TextView address = v.findViewById(android.R.id.text2); // Set address
                TextView dist = v.findViewById(R.id.distance); // Set distance

                if (c != null) {
                    name.setText(c.getName());
                    address.setText(c.getAddress());
                    dist.setText(Utils.formatDist(c.getDistance()));
                }

                return v;
            }
        };
        Spinner spinner = findViewById(R.id.spinnerToolbar);
        spinner.setAdapter(adapterCafeterias);
        spinner.setOnItemSelectedListener(this);
        //TODO: handle on empty, cause right now we no longer handle no internet connection or empty results at all
        //TODO: only select first item on activity start, but not on database update.
        mDisposable.add(cafeteriaViewModel.getAllCafeteria(new LocationManager(this).getCurrentOrNextLocation())
                                          .subscribe(
                                                  it -> {
                                                      mCafeterias.clear();
                                                      mCafeterias.addAll(it);
                                                      adapterCafeterias.notifyDataSetChanged();
                                                      int selIndex = -1;
                                                      for (int i = 0; i < mCafeterias.size(); i++) {
                                                          Cafeteria c = mCafeterias.get(i);
                                                          if (mCafeteriaId == -1 || mCafeteriaId == c.getId()) {
                                                              mCafeteriaId = c.getId();
                                                              selIndex = i;
                                                              break;
                                                          }
                                                      }
                                                      if (selIndex > -1) {
                                                          spinner.setSelection(selIndex);
                                                      }
                                                  }, throwable -> Utils.logwithTag("CafeteriaActivity", throwable.getMessage())
                                          ));

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
        Intent intent = getIntent();
        //check if Activity triggered from favoriteDish Notification
        if (intent != null && intent.getExtras() != null && intent.getExtras()
                                                                  .containsKey(Const.MENSA_FOR_FAVORITEDISH)) {
            for (int i = 0; i < parent.getCount(); i++) {
                //get mensaId from extra to redirect the user to it.
                if (intent.getExtras()
                          .getInt(Const.MENSA_FOR_FAVORITEDISH) == mCafeterias.get(i)
                                                                              .getId()) {
                    mCafeteriaId = mCafeterias.get(i)
                                              .getId();
                    parent.setSelection(i);
                    intent.removeExtra(Const.MENSA_FOR_FAVORITEDISH);
                    break;
                }
            }
        } else {
            mCafeteriaId = mCafeterias.get(pos)
                                      .getId();
        }

        CafeteriaDetailsSectionsPagerAdapter mSectionsPagerAdapter
                = new CafeteriaDetailsSectionsPagerAdapter(getSupportFragmentManager());
        // Create the adapter that will return a fragment for each of the primary sections of the app.
        mViewPager.setAdapter(null); //unset the adapter for updating
        mSectionsPagerAdapter.setCafeteriaId(this, mCafeteriaId);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Don't change anything
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }
}