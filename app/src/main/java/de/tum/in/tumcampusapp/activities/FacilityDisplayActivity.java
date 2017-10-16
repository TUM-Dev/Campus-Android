package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Optional;
import java.io.IOException;

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
import de.tum.in.tumcampusapp.models.tumcabe.FacilityVote;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the map regarding the searched room.
 */
public class FacilityDisplayActivity extends ActivityForLoadingInBackground<Integer, Optional<Facility>>{
    private ImageViewTouchFragment mImage;

    private NetUtils net;
    private Fragment fragment;
    private Facility facility;

    public static final String FACILITY_ID ="facility_id";

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
        upvoteButton=(Button)findViewById(R.id.upvote);
        upvoteButton.setEnabled(false);
        downvoteButton=(Button)findViewById(R.id.downvote);
        downvoteButton.setEnabled(false);

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFacilityVote(1);

            }
        });
        downvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFacilityVote(-1);
            }
        });

        mImage = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.facility_location_map, mImage).commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            startLoading((Integer) bundle.getSerializable(FACILITY_ID));
        }
    }

    private void saveFacilityVote(Integer vote) {
        try {
            final ChatVerification v = new ChatVerification(this, Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class));
            TUMCabeClient.getInstance(this).saveFacilityVote(facility.getId(),vote,v, new Callback<Facility>() {
                @Override
                public void onResponse(Call<Facility> call, Response<Facility> response) {
                    if (!response.isSuccessful()) {
                        Utils.logv("Error saving vote: " + response.message());
                        Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_votes_failure);
                        return;
                    }
                    facility=response.body();
                    updateButtons();
                    Utils.showToastOnUIThread(FacilityDisplayActivity.this, R.string.facility_vote_save_success);
                }
                @Override
                public void onFailure(Call<Facility> call, Throwable throwable) {
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

    @Override
    public void onBackPressed() {
        if (fragment != null) {
            supportInvalidateOptionsMenu();
            return;
        }

        super.onBackPressed();
    }


    @Override
    protected Optional<Facility> onLoadInBackground(Integer... arg) {
        try {
            Facility facility = TUMCabeClient.getInstance(this).getFacilityById(arg[0]);
            return Optional.fromNullable(facility);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    @Override
    protected void onLoadFinished(Optional<Facility> result) {
        if (!result.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }

        this.facility=result.get();
        updateButtons();

        supportInvalidateOptionsMenu();
        //Update the fragment
        mImage = ImageViewTouchFragment.newInstance(net.getFacilityMapImage(Optional.of(result.get().getMap())).get());
        getSupportFragmentManager().beginTransaction().replace(R.id.facility_location_map, mImage).commit();
        showLoadingEnded();
    }

    private void updateButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                upvoteButton.setText("Upvote("+facility.getUpVotes()+")");
                downvoteButton.setText("Downvote("+facility.getDownVotes()+")");
                if(facility.getMyVote()==null){
                    upvoteButton.setEnabled(true);
                    downvoteButton.setEnabled(true);
                }
                else if(facility.getMyVote()==1){
                    upvoteButton.setEnabled(false);
                    downvoteButton.setEnabled(true);
                }
                else{
                    upvoteButton.setEnabled(true);
                    downvoteButton.setEnabled(false);
                }
            }
        });
    }
}
