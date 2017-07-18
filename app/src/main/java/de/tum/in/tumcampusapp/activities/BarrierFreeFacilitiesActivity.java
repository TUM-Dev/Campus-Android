package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import de.tum.in.tumcampusapp.R;

import de.tum.in.tumcampusapp.activities.generic.BaseActivity;

public class BarrierFreeFacilitiesActivity extends BaseActivity implements AdapterView.OnItemSelectedListener{
    private int selectedFacilityPage = -1;

    public BarrierFreeFacilitiesActivity(){
        super(R.layout.activity_barrier_free_facilities);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activate action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // set spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFacilityPage = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing seleced
    }
}
