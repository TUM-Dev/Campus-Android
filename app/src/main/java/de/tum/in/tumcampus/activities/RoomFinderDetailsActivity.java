package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.auxiliary.calendar.DayView;
import de.tum.in.tumcampus.auxiliary.calendar.EventLoader;
import de.tum.in.tumcampus.auxiliary.calendar.LoadEventsRequestTimeTable;
import de.tum.in.tumcampus.fragments.DayFragment;
import de.tum.in.tumcampus.models.Geo;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Displays the map regarding the searched room.
 */
public class RoomFinderDetailsActivity extends ActivityForLoadingInBackground<Void, Bitmap> implements DialogInterface.OnClickListener {

    public static final String EXTRA_ROOM_INFO = "roomInfo";
    public static final String EXTRA_LOCATION = "location";

    private ImageViewTouch mImage;

    private boolean mapsLoaded = false;
    private TUMRoomFinderRequest request;
    private NetUtils net;

    private String location;
    private Bundle roomInfo;
    private String mapId = "";
    private ArrayList<HashMap<String, String>> mapsList;
    private boolean infoLoaded = false;
    private DayFragment fragment;

    public RoomFinderDetailsActivity() {
        super(R.layout.activity_roomfinderdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        mImage = (ImageViewTouch) findViewById(R.id.activity_roomfinder_details);
        mImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        mImage.setDoubleTapEnabled(false);

        location = getIntent().getExtras().getString(EXTRA_LOCATION);
        roomInfo = getIntent().getExtras().getBundle(EXTRA_ROOM_INFO);
        request = new TUMRoomFinderRequest(this);

        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_roomfinder_detail, menu);
        MenuItem switchMap = menu.findItem(R.id.action_switch_map);
        switchMap.setVisible(!mapId.equals("10") && mapsLoaded && fragment == null);
        MenuItem timetable = menu.findItem(R.id.action_room_timetable);
        timetable.setVisible(infoLoaded);
        timetable.setIcon(fragment == null ? R.drawable.ic_room_timetable : R.drawable.ic_action_map);
        menu.findItem(R.id.action_directions).setVisible(infoLoaded && fragment == null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_room_timetable:
                getRoomTimetable();
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_directions:
                getDirections();
                return true;
            case R.id.action_switch_map:
                showMapSwitch();
                return true;
            default:
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
            ft.remove(fragment);
            ft.commit();
            fragment = null;
            return;
        }
        Time t = new Time();
        t.setToNow();
        DayView.mLeftBoundary = Time.getJulianDay(t.toMillis(true), t.gmtoff);
        LoadEventsRequestTimeTable req = new LoadEventsRequestTimeTable(roomInfo.getString(TUMRoomFinderRequest.KEY_ROOM_API_CODE));
        fragment = new DayFragment(0, 1, new EventLoader(this, req));
        ft.add(android.R.id.content, fragment);
        ft.commit();
    }

    private void showMapSwitch() {
        CharSequence[] list = new CharSequence[mapsList.size()];
        int curPos = 0;
        for (int i = 0; i < mapsList.size(); i++) {
            list[i] = mapsList.get(i).get(TUMRoomFinderRequest.KEY_TITLE);
            if (mapsList.get(i).get(TUMRoomFinderRequest.KEY_MAP_ID).equals(mapId)) {
                curPos = i;
            }
        }
        new AlertDialog.Builder(this).setSingleChoiceItems(list, curPos, this).show();
    }

    public void onClick(DialogInterface dialog, int whichButton) {
        dialog.dismiss();
        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        mapId = mapsList.get(selectedPosition).get(TUMRoomFinderRequest.KEY_MAP_ID);
        startLoading();
    }

    private void getDirections() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Geo geo = request.fetchCoordinates(roomInfo.getString(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER));
                if (geo == null) {
                    Utils.showToastOnUIThread(RoomFinderDetailsActivity.this, R.string.no_map_available);
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Build get directions intent and see if some app can handle it
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + geo.getLatitude() + "," + geo.getLongitude()));
                        List<ResolveInfo> pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);

                        // If some app can handle this intent start it
                        if (pkgAppsList.size() > 0) {
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
                });
            }
        }).start();
    }

    @Override
    protected Bitmap onLoadInBackground(Void... arg) {
        TUMRoomFinderRequest requestHandler = new TUMRoomFinderRequest(this);

        // First search for roomId and mapId
        if (location != null && !location.isEmpty() && roomInfo == null) {
            ArrayList<HashMap<String, String>> request = requestHandler.fetchRooms(location);
            if (request.size() > 0) {
                HashMap<String, String> room = request.get(0);

                roomInfo = new Bundle();
                for (Map.Entry<String, String> entry : room.entrySet()) {
                    roomInfo.putString(entry.getKey(), entry.getValue());
                }
            }
        }

        if (roomInfo == null)
            return null;

        if (mapId == null || mapId.isEmpty()) {
            mapId = requestHandler.fetchDefaultMapId(roomInfo.getString(TUMRoomFinderRequest.KEY_BUILDING_ID));
        }

        String url = null;
        try {
            String roomId = roomInfo.getString(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER);
            if (roomId == null) {
                return null;
            }
            if (mapId.equals("10")) {
                url = "http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/building/defaultMap?id="
                        + URLEncoder.encode(roomId.substring(roomId.indexOf('@') + 1), "UTF-8");
            } else {
                url = "http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/map?id="
                        + URLEncoder.encode(roomId, "UTF-8") + "&mapid="
                        + URLEncoder.encode(mapId, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            Utils.log(e);
        }

        if (url == null)
            return null;

        return net.downloadImageToBitmap(url);
    }

    @Override
    protected void onLoadFinished(Bitmap result) {
        if (result == null) {
            if (!NetUtils.isConnected(this)) {
                showNoInternetLayout();
            } else {
                showErrorLayout();
            }
            return;
        }
        infoLoaded = true;
        supportInvalidateOptionsMenu();
        mImage.setImageBitmap(result);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(roomInfo.getString(TUMRoomFinderRequest.KEY_ROOM_TITLE));
            getSupportActionBar().setSubtitle(roomInfo.getString(TUMRoomFinderRequest.KEY_BUILDING_TITLE));
        }
        showLoadingEnded();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mapsList = request.fetchAvailableMaps(roomInfo.getString(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER));
                if (mapsList.size() > 1) {
                    mapsLoaded = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            supportInvalidateOptionsMenu();
                        }
                    });
                }
            }
        }).start();
    }
}
