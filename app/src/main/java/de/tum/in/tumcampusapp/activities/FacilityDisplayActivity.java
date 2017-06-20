package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.common.base.Optional;

import java.io.File;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;

import de.tum.in.tumcampusapp.fragments.ImageViewTouchFragment;

/**
 * Displays the map regarding the searched room.
 */
public class FacilityDisplayActivity extends ActivityForLoadingInBackground<Void, Optional<File>>{
    private ImageViewTouchFragment mImage;

    private NetUtils net;
    private Fragment fragment;
    private double longitude,latitude;
    private String facilityName;

    public FacilityDisplayActivity() {
        super(R.layout.activity_facility_display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);

        mImage = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.facility_location_map, mImage).commit();

        longitude = getIntent().getExtras().getDouble(FacilityActivity.LONGITUDE);
        latitude = getIntent().getExtras().getDouble(FacilityActivity.LATITUDE);
        facilityName=getIntent().getExtras().getString(FacilityActivity.FACILITY_NAME);
        setTitle(facilityName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        startLoading();
    }


    @Override
    public void onBackPressed() {
        if (fragment != null) {
            supportInvalidateOptionsMenu();
            return;
        }

        super.onBackPressed();
    }


    @Override
    protected Optional<File> onLoadInBackground(Void... arg) {
        return net.getFacilityMapImage(facilityName,longitude,latitude);
    }


    @Override
    protected void onLoadFinished(Optional<File> result) {
        if (!result.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }
        supportInvalidateOptionsMenu();

        //Update the fragment
        mImage = ImageViewTouchFragment.newInstance(result.get());
        getSupportFragmentManager().beginTransaction().replace(R.id.facility_location_map, mImage).commit();
        showLoadingEnded();
    }
}
