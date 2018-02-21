package de.tum.in.tumcampusapp.component.tumui.roomfinder;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.Helper;
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
        extends ActivityForLoadingInBackground<Void, Optional<File>>
        implements DialogInterface.OnClickListener {

    public static final String EXTRA_ROOM_INFO = "roomInfo";
    public static final String EXTRA_LOCATION = "location";

    private ImageViewTouchFragment mImage;

    private boolean mapsLoaded;
    private NetUtils net;

    private RoomFinderRoom room;
    private String mapId = "";
    private List<RoomFinderMap> mapsList;
    private boolean infoLoaded;
    private Fragment fragment;

    public RoomFinderDetailsActivity() {
        super(R.layout.activity_roomfinderdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);

        mImage = ImageViewTouchFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                                   .add(R.id.fragment_container, mImage)
                                   .commit();

        room = (RoomFinderRoom) getIntent().getExtras()
                                           .getSerializable(EXTRA_ROOM_INFO);
        if (room == null) {
            Utils.showToast(this, "No room information passed");
            this.finish();
            return;
        }

        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_roomfinder_detail, menu);
        MenuItem switchMap = menu.findItem(R.id.action_switch_map);
        switchMap.setVisible(!"10".equals(mapId) && mapsLoaded && fragment == null);
        MenuItem timetable = menu.findItem(R.id.action_room_timetable);
        timetable.setVisible(infoLoaded);
        timetable.setIcon(fragment == null ? R.drawable.ic_room_timetable : R.drawable.ic_action_map);
        menu.findItem(R.id.action_directions)
            .setVisible(infoLoaded && fragment == null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            ft.replace(R.id.fragment_container, mImage);
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
        int selectedPosition = ((AlertDialog) dialog).getListView()
                                                     .getCheckedItemPosition();
        mapId = mapsList.get(selectedPosition)
                        .getMap_id();
        startLoading();
    }

    @Override
    protected Optional<File> onLoadInBackground(Void... arg) {
        String archId = room.getArch_id();
        String url;

        if (mapId == null || mapId.isEmpty()) {
            url = Const.URL_DEFAULT_MAP_IMAGE + Helper.encodeUrl(archId);
        } else {
            url = Const.URL_MAP_IMAGE + Helper.encodeUrl(archId) + '/' + Helper.encodeUrl(mapId);
        }

        return net.downloadImage(url);
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
        infoLoaded = true;
        supportInvalidateOptionsMenu();

        //Update the fragment
        mImage = ImageViewTouchFragment.newInstance(result.get());
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, mImage)
                                   .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(room.getInfo());
            getSupportActionBar().setSubtitle(room.getAddress());
        }

        showLoadingEnded();

        loadMapList();
    }

    private void loadMapList() {
        showLoadingStart();
        try {
            TUMCabeClient.getInstance(this)
                         .fetchAvailableMaps(room.getArch_id(), new Callback<List<RoomFinderMap>>() {
                             @Override
                             public void onResponse(Call<List<RoomFinderMap>> call, Response<List<RoomFinderMap>> response) {
                                 try {
                                     onMapListLoadFinished(Optional.of(response.body()));
                                 } catch (NullPointerException e) {
                                     Utils.log(e);
                                     onMapListLoadFailed();
                                 }
                             }

                             @Override
                             public void onFailure(Call<List<RoomFinderMap>> call, Throwable throwable) {
                                 onMapListLoadFailed();
                             }
                         });
        } catch (IOException e) {
            Utils.log(e);
            onMapListLoadFailed();
        }
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
        try {
            TUMCabeClient.getInstance(this)
                         .fetchCoordinates(room.getArch_id(), new Callback<RoomFinderCoordinate>() {
                             @Override
                             public void onResponse(Call<RoomFinderCoordinate> call, Response<RoomFinderCoordinate> response) {
                                 try {
                                     Optional<Geo> result = LocationManager.convertRoomFinderCoordinateToGeo(response.body());
                                     onGeoLoadFinished(result);
                                 } catch (NullPointerException e) {
                                     Utils.log(e);
                                     onLoadGeoFailed();
                                 }
                             }

                             @Override
                             public void onFailure(Call<RoomFinderCoordinate> call, Throwable throwable) {
                                 onLoadGeoFailed();
                             }
                         });
        } catch (IOException e) {
            Utils.log(e);
            onLoadGeoFailed();
        }
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
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + result.get()
                                                                                                .getLatitude() + ',' + result.get()
                                                                                                                             .getLongitude()));
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
}
