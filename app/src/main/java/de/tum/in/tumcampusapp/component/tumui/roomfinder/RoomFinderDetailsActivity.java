package de.tum.in.tumcampusapp.component.tumui.roomfinder;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.base.Optional;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.ApiHelper;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.ImageViewTouchFragment;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.other.locations.model.Geo;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderMap;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the map regarding the searched room.
 */
public class RoomFinderDetailsActivity
        extends ActivityForLoadingInBackground<Void, String>
        implements DialogInterface.OnClickListener {

    public static final String EXTRA_ROOM_INFO = "roomInfo";

    private ImageViewTouchFragment mImageFragment;

    private boolean mapsLoaded;

    private RoomFinderRoom room;
    private String mapId = "";
    private List<RoomFinderMap> mapsList;
    private boolean infoLoaded;

    private Fragment fragment;

    private Call<RoomFinderCoordinate> mRoomFinderCoordinateCall;
    private Call<List<RoomFinderMap>> mRoomFinderMapsCall;

    public RoomFinderDetailsActivity() {
        super(R.layout.activity_roomfinderdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageFragment = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                                   .add(R.id.fragment_container, mImageFragment)
                                   .commit();

        room = (RoomFinderRoom) getIntent().getSerializableExtra(EXTRA_ROOM_INFO);
        if (room == null) {
            Utils.showToast(this, "No room information passed");
            finish();
            return;
        }

        mImageFragment = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                                   .add(R.id.fragment_container, mImageFragment)
                                   .commit();

        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_roomfinder_detail, menu);
        MenuItem switchMap = menu.findItem(R.id.action_switch_map);
        switchMap.setVisible(!"10".equals(mapId) && mapsLoaded && fragment == null);
        MenuItem timetable = menu.findItem(R.id.action_room_timetable);
        timetable.setVisible(infoLoaded);
        timetable.setIcon(fragment == null ? R.drawable.ic_outline_event_note_24px : R.drawable.ic_outline_map_24px);
        menu.findItem(R.id.action_directions)
            .setVisible(infoLoaded && fragment == null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_room_timetable) {
            getRoomTimetable();
            supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.action_directions) {
            loadGeo();
            return true;
        } else if (i == R.id.action_switch_map) {
            showMapSwitch();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Remove fragment with room timetable if present and show map again
        if (fragment != null) {
            getRoomTimetable();
            supportInvalidateOptionsMenu();
            return;
        }

        super.onBackPressed();
    }

    private void getRoomTimetable() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Remove if fragment is already present
        if (fragment != null) {
            ft.replace(R.id.fragment_container, mImageFragment);
            ft.commit();
            fragment = null;
            return;
        }

        String roomApiCode = room.getRoom_id();
        fragment = WeekViewFragment.newInstance(roomApiCode);
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    private void showMapSwitch() {
        CharSequence[] list = new CharSequence[mapsList.size()];
        int curPos = 0;
        for (int i = 0; i < mapsList.size(); i++) {
            list[i] = mapsList.get(i)
                              .getDescription();
            if (mapsList.get(i)
                        .getMap_id()
                        .equals(mapId)) {
                curPos = i;
            }
        }
        new AlertDialog.Builder(this).setSingleChoiceItems(list, curPos, this)
                                     .show();
    }

    @Override
    public void onClick(DialogInterface dialog, int whichButton) {
        dialog.dismiss();
        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        mapId = mapsList.get(selectedPosition).getMap_id();
        startLoading();
    }

    @Override
    protected String onLoadInBackground(Void... arg) {
        String archId = room.getArch_id();
        String url;
        if (mapId == null || mapId.isEmpty()) {
            url = Const.URL_DEFAULT_MAP_IMAGE + ApiHelper.encodeUrl(archId);
        } else {
            url = Const.URL_MAP_IMAGE + ApiHelper.encodeUrl(archId) + '/' + ApiHelper.encodeUrl(mapId);
        }
        return url;
    }

    @Override
    protected void onLoadFinished(String url) {
        mImageFragment.loadImage(url, this::showImageLoadingError);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(room.getInfo());
            getSupportActionBar().setSubtitle(room.getFormattedAddress());
        }

        showLoadingEnded();
        loadMapList();
    }

    private void loadMapList() {
        showLoadingStart();

        mRoomFinderMapsCall = TUMCabeClient.getInstance(this).fetchAvailableMaps(room.getArch_id());
        mRoomFinderMapsCall.enqueue(new Callback<List<RoomFinderMap>>() {
            @Override
            public void onResponse(@NonNull Call<List<RoomFinderMap>> call,
                                   @NonNull Response<List<RoomFinderMap>> response) {
                List<RoomFinderMap> data = response.body();
                mRoomFinderMapsCall = null;

                if (!response.isSuccessful() || data == null) {
                    onMapListLoadFailed();
                    return;
                }

                onMapListLoadFinished(Optional.of(data));
            }

            @Override
            public void onFailure(@NonNull Call<List<RoomFinderMap>> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled()) {
                    return;
                }

                onMapListLoadFailed();
                mRoomFinderMapsCall = null;
            }
        });
    }

    private void onMapListLoadFailed() {
        onMapListLoadFinished(Optional.absent());
    }

    private void onMapListLoadFinished(Optional<List<RoomFinderMap>> result) {
        showLoadingEnded();
        if (!result.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }
        mapsList = result.get();
        if (mapsList.size() > 1) {
            mapsLoaded = true;
        }

        supportInvalidateOptionsMenu();
    }

    private void loadGeo() {
        showLoadingStart();
        mRoomFinderCoordinateCall = TUMCabeClient.getInstance(this)
                .fetchRoomFinderCoordinates(room.getArch_id());
        mRoomFinderCoordinateCall.enqueue(new Callback<RoomFinderCoordinate>() {
            @Override
            public void onResponse(@NonNull Call<RoomFinderCoordinate> call,
                                   @NonNull Response<RoomFinderCoordinate> response) {
                RoomFinderCoordinate data = response.body();
                mRoomFinderCoordinateCall = null;

                if (!response.isSuccessful() || data == null) {
                    onLoadGeoFailed();
                    return;
                }

                onGeoLoadFinished(LocationManager.Companion.convertRoomFinderCoordinateToGeo(data));
            }

            @Override
            public void onFailure(@NonNull Call<RoomFinderCoordinate> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled()) {
                    return;
                }

                onLoadGeoFailed();
                mRoomFinderCoordinateCall = null;
            }
        });
    }

    private void onLoadGeoFailed() {
        onGeoLoadFinished(Optional.absent());
    }

    private void onGeoLoadFinished(Optional<Geo> result) {
        showLoadingEnded();
        if (!result.isPresent()) {
            Utils.showToastOnUIThread(RoomFinderDetailsActivity.this, R.string.no_map_available);
            return;
        }

        // Build get directions intent and see if some app can handle it
        String coordinates = result.get().getLatitude() + ',' + result.get().getLongitude();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + coordinates));
        List<ResolveInfo> pkgAppsList = getApplicationContext().getPackageManager()
                .queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);

        // If some app can handle this intent start it
        if (!pkgAppsList.isEmpty()) {
            startActivity(intent);
            return;
        }

        // If no app is capable of opening it link to google maps market entry
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")));
        } catch (ActivityNotFoundException e) {
            Utils.log(e);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps")));
        }
    }

    private void showImageLoadingError() {
        if (NetUtils.isConnected(this)) {
            showError(R.string.error_something_wrong);
        } else {
            showNoInternetLayout();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mRoomFinderMapsCall != null) {
            mRoomFinderMapsCall.cancel();
        }

        if (mRoomFinderCoordinateCall != null) {
            mRoomFinderCoordinateCall.cancel();
        }
    }
}
