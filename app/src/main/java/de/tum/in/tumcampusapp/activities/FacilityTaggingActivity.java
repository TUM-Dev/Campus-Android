package de.tum.in.tumcampusapp.activities;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.common.base.Optional;
import java.io.File;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.fragments.ImageViewTouchFragment;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.tumonline.TUMFacilityLocatorRequest;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest;

/**
 * Displays the map regarding the searched room.
 */
public class FacilityTaggingActivity extends ActivityForLoadingInBackground<Void, Optional<File>> implements DialogInterface.OnClickListener {


    private ImageViewTouchFragment mImage;
    private TUMRoomFinderRequest request;
    private NetUtils net;
    private Fragment fragment;

    private Button saveFacilityButton;

    public FacilityTaggingActivity() {
        super(R.layout.activity_facility_tagging);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);

        mImage = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.current_location_map, mImage).commit();

        Spinner spinner = (Spinner) findViewById(R.id.facility_categories_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.facility_categories, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        saveFacilityButton = (Button) findViewById(R.id.save_facility);
        saveFacilityButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFacility();
            }
        });
        startLoading();
    }

    private void saveFacility() {
//        findViewById(R.id.save_facility).getVa
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
    public void onClick(DialogInterface dialog, int whichButton) {
        dialog.dismiss();
        startLoading();
    }



    @Override
    protected Optional<File> onLoadInBackground(Void... arg) {
        Location currentLocation=new LocationManager(this).getCurrentLocation();
        String map= TUMFacilityLocatorRequest.getMapWithLocation(currentLocation.getLongitude(), currentLocation.getLatitude());
        if(map==null){
            Utils.showToast(this, "Your current location is not in any university campus");
            this.finish();
            return null;
        }
        else{
            return net.saveCurrentLocationImage(map);
        }
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
        getSupportFragmentManager().beginTransaction().replace(R.id.current_location_map, mImage).commit();
        showLoadingEnded();
    }
}
