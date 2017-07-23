package de.tum.in.tumcampusapp.activities;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.common.base.Optional;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.fragments.ImageViewTouchFragment;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumcabe.Facility;
import de.tum.in.tumcampusapp.models.tumcabe.FacilityCategory;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the map regarding the searched room.
 */
public class FacilityTaggingActivity extends ActivityForLoadingInBackground<Void, Optional<File>> implements DialogInterface.OnClickListener{


    private ImageViewTouchFragment mImage;
    private TUMRoomFinderRequest request;
    private NetUtils net;
    private Fragment fragment;

    private FloatingActionButton saveFacilityButton;


    ArrayAdapter<String> spinnerAdapter;
    Map<String,Integer> options;

    public static final String EDIT_MODE="editMode";
    public static final String FACILITY="facility";
    private boolean editMode;

    public Facility facility;
    private ChatMember chatMember;


    public FacilityTaggingActivity() {
        super(R.layout.activity_facility_tagging);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);

        chatMember=Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            editMode=bundle.getBoolean(EDIT_MODE);
            if(editMode){
                facility= (Facility) bundle.getSerializable(FACILITY);
            }
            else{
                facility=new Facility();
                String lrzId=Utils.getSetting(FacilityTaggingActivity.this, Const.LRZ_ID, "");
                if(lrzId==null || lrzId.isEmpty()){
                    Utils.logv("User not logged in while adding new facility");
                    Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_log_in_error);
                    finish();
                }
                Location currentLocation=new LocationManager(FacilityTaggingActivity.this).getCurrentLocation();
                facility.setLatitude(currentLocation.getLatitude());
                facility.setLongitude(currentLocation.getLongitude());
                facility.setCreatedBy(lrzId);
            }
        }
        
        mImage = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.current_location_map, mImage).commit();

        initForm();

        saveFacilityButton = (FloatingActionButton) findViewById(R.id.save_facility);
        saveFacilityButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFacility();
            }
        });
        startLoading();
    }

    private void initForm() {
        Spinner spinner = (Spinner) findViewById(R.id.facility_categories_spinner);
        spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        try {
            TUMCabeClient.getInstance(this).getFacilityCategories(new Callback<List<FacilityCategory>>() {
                @Override
                public void onResponse(Call<List<FacilityCategory>> call, Response<List<FacilityCategory>> response) {
                    if (!response.isSuccessful()) {
                        Utils.logv("Error getting facility categories: " + response.message());
                        return;
                    }
                    List<FacilityCategory> facilityCategories=response.body();
                    if(facilityCategories!=null && facilityCategories.size()>0){
                        spinnerAdapter.clear();
                        options = new HashMap<>();
                        for (FacilityCategory facilityCategory: facilityCategories) {
                            spinnerAdapter.add(facilityCategory.getName());
                            options.put(facilityCategory.getName(), facilityCategory.getId());
                        }
                        if(editMode){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    EditText editText=(EditText) FacilityTaggingActivity.this.findViewById(R.id.facility_name);
                                    int position=spinnerAdapter.getPosition(facility.getFacilityCategory().getName());
                                    Spinner spinner=(Spinner) FacilityTaggingActivity.this.findViewById(R.id.facility_categories_spinner);
                                    editText.setText(facility.getName());
                                    spinner.setSelection(position);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<FacilityCategory>> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting facility categories from the server");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFacility() {
        String facilityName = ((EditText) findViewById(R.id.facility_name)).getText().toString();
        if(facilityName==null || facilityName.isEmpty()){
            Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_name_empty_error);
            return;
        }
        String categoryName= ((Spinner)findViewById(R.id.facility_categories_spinner)).getSelectedItem().toString();
        Integer categoryId=options.get(categoryName);
        facility.setName(facilityName);
        facility.setFacilityCategory(new FacilityCategory(categoryId));
        try {
            final ChatVerification v = new ChatVerification(this, Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class));
            TUMCabeClient.getInstance(this).saveFacility(facility,v, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        try {
                            Utils.logv("Error saving Facility: " + response.message());
                            Utils.showToastOnUIThread(FacilityTaggingActivity.this, response.errorBody().string());
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_save_success);
                    FacilityTaggingActivity.this.finish();
                }
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Utils.log(throwable, "Failure saving facility");
                    Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_save_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoPrivateKey noPrivateKey) {
            noPrivateKey.printStackTrace();
        }
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
        return net.getFacilityMapImage(this,facility.getName(),facility.getLongitude(), facility.getLatitude());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(editMode){
            getMenuInflater().inflate(R.menu.menu_facility, menu);
            MenuItem deleteFacility = menu.findItem(R.id.action_delete_facility);
            deleteFacility.setVisible(true);
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_delete_facility) {
            deleteFacility();
            return true;
        }
        return false;
    }

    private void deleteFacility() {
        try {
            ChatVerification verification = new ChatVerification(FacilityTaggingActivity.this, chatMember);
            TUMCabeClient.getInstance(this).deleteFacility(facility.getId(),verification, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        try {
                            String error= response.errorBody().string();
                            System.out.print(error);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Utils.logv("Error deleting Facility: " + response.message());
                        Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_delete_failure);
                        return;
                    }
                    Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_delete_success);
                    FacilityTaggingActivity.this.finish();
                }
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Utils.log(throwable, "Failure deleting facility");
                    Utils.showToastOnUIThread(FacilityTaggingActivity.this, R.string.facility_delete_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (NoPrivateKey noPrivateKey) {
            return; //In this case we simply cannot do anything
        }
    }
}
