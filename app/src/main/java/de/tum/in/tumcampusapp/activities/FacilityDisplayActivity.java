package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.fragments.ImageViewTouchFragment;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumcabe.Facility;
import de.tum.in.tumcampusapp.models.tumcabe.FacilityMap;
import de.tum.in.tumcampusapp.models.tumcabe.FacilityVote;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the map regarding the searched room.
 */
public class FacilityDisplayActivity extends ActivityForLoadingInBackground<Void, Optional<File>>{
    private ImageViewTouchFragment mImage;

    private NetUtils net;
    private Fragment fragment;
    private double longitude,latitude;
    private String facilityName;
    private Facility facility;

    public static final String FACILITY="facility";

    private Button upvoteButton;
    private Button downvoteButton;

    private FacilityVote facilityVote;

    
    public FacilityDisplayActivity() {
        super(R.layout.activity_facility_display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        net = new NetUtils(this);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            facility= (Facility) bundle.getSerializable(FACILITY);
            longitude = facility.getLongitude();
            latitude = facility.getLatitude();
            facilityName=facility.getName();
            setTitle(facilityName);
        }

        upvoteButton=(Button)findViewById(R.id.upvote);
        downvoteButton=(Button)findViewById(R.id.downvote);


        upvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFacilityVote(true);

            }
        });

        downvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFacilityVote(false);
            }
        });


        mImage = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.facility_location_map, mImage).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        startLoading();
    }

    private void saveFacilityVote(boolean vote) {
        facilityVote.setVote(vote);

        try {
            final ChatVerification v = new ChatVerification(this, Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class));
            TUMCabeClient.getInstance(this).saveFacilityVote(facility.getId(),facilityVote,v, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        try {
                            String error= response.errorBody().string();
                            System.out.print(error);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Utils.logv("Error saving vote: " + response.message());
                        Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                        return;
                    }
                    Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_vote_save_success);
                    fetchVotes();
                    fetchVotingStatus();
                }
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Utils.log(throwable, "Failure saving vote");
                    Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (NoPrivateKey noPrivateKey) {
            noPrivateKey.printStackTrace();
        }
    }

    private void fetchVotes() {

        try {
            TUMCabeClient.getInstance(this).getFacilityVotes(facility.getId(), new Callback<Integer[]>() {
                @Override
                public void onResponse(Call<Integer[]> call, Response<Integer[]> response) {
                    if (!response.isSuccessful()) {
                        try {
                            String error= response.errorBody().string();
                            System.out.print(error);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Utils.logv("Error getting votes: " + response.message());
                        Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                        return;
                    }
                    final Integer[] votes=response.body();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            upvoteButton.setText("Upvote("+votes[0]+")");
                            downvoteButton.setText("Downvote("+votes[1]+")");
                        }
                    });
                }
                @Override
                public void onFailure(Call<Integer[]> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting votes");
                    Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
    protected Optional<File> onLoadInBackground(Void... arg) {

        fetchVotes();
        fetchVotingStatus();

        return net.getFacilityMapImage(this,facilityName,longitude,latitude);

    }

    private void fetchVotingStatus() {
        final String lrzId=Utils.getSetting(this, Const.LRZ_ID, "");
        try {
            TUMCabeClient.getInstance(this).getFacilityVote(facility.getId(),lrzId, new Callback<FacilityVote>() {
                @Override
                public void onResponse(Call<FacilityVote> call, Response<FacilityVote> response) {
                    if (!response.isSuccessful()) {
                        try {
                            String error= response.errorBody().string();
                            System.out.print(error);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Utils.logv("Error getting vote status " + response.message());
                        Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                        return;
                    }
                    facilityVote=response.body();

                    if(facilityVote==null || facilityVote.getId()==0){
                        facilityVote=new FacilityVote();
                        facilityVote.setFacility(facility);
                        facilityVote.setUser(lrzId);
                        updateButtons(true,true);
                    }
                    else{
                        updateButtons(!facilityVote.isVote(),facilityVote.isVote());
                    }

                }
                @Override
                public void onFailure(Call<FacilityVote> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting vote status");
                    Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void updateButtons(final boolean upvote, final boolean downvote){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            upvoteButton.setEnabled(upvote);
            downvoteButton.setEnabled(downvote);

            }
        });
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
