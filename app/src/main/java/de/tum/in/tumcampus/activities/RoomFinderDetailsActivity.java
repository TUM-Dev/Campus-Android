package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Geo;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Displays the map regarding the searched room.
 */
public class RoomFinderDetailsActivity extends ActivityForLoadingInBackground<Void, Bitmap> implements DialogInterface.OnClickListener {

    public static final String BUILDING_ID = "buildingId";
    public static final String BUILDING_TITLE = "buildingTitle";
    public static final String ROOM_ID = "roomId";
    public static final String CAMPUS_TITLE = "campusTitle";
    public static final String ROOM_TITLE = "roomTitle";
    public static final String EXTRA_ROOM_INFO = "roomInfo";
    public static final String EXTRA_LOCATION = "location";

    private ImageViewTouch mImage;
    private TextView mDetails1;
    private TextView mDetails2;

    private boolean mapsLoaded = false;
    private TUMRoomFinderRequest request;
    private NetUtils net;

    private String location;
    private Bundle roomInfo;
    private String mapId = "";
    private ArrayList<HashMap<String, String>> mapsList;

    public RoomFinderDetailsActivity() {
        super(R.layout.activity_roomfinderdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        mImage = (ImageViewTouch) findViewById(R.id.activity_roomfinder_details);
        mImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        mDetails1 = (TextView)findViewById(R.id.textView);
        mDetails2 = (TextView)findViewById(R.id.textView2);

        location = getIntent().getExtras().getString(EXTRA_LOCATION);
        roomInfo = getIntent().getExtras().getBundle(EXTRA_ROOM_INFO);
        request = new TUMRoomFinderRequest();

        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_roomfinder_detail, menu);
        MenuItem switchMap = menu.findItem(R.id.action_switch_map);
        switchMap.setVisible(!mapId.equals("10") && mapsLoaded);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    private void showMapSwitch() {
        CharSequence[] list = new CharSequence[mapsList.size()];
        int curPos = 0;
        for (int i = 0; i < mapsList.size(); i++) {
            list[i] = mapsList.get(i).get(TUMRoomFinderRequest.KEY_TITLE);
            if (mapsList.get(i).get(TUMRoomFinderRequest.KEY_ID).equals(mapId)) {
                curPos = i;
            }
        }
        new AlertDialog.Builder(this).setSingleChoiceItems(list, curPos, this).show();
    }

    public void onClick(DialogInterface dialog, int whichButton) {
        dialog.dismiss();
        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        mapId = mapsList.get(selectedPosition).get(TUMRoomFinderRequest.KEY_ID);
        startLoading();
    }

    private void getDirections() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Geo geo = request.fetchCoordinates(roomInfo.getString(ROOM_ID));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (geo == null) {
                            Utils.showToast(RoomFinderDetailsActivity.this, R.string.no_map_available);
                        } else {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +
                                    geo.getLatitude() + "," + geo.getLongitude())));
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected Bitmap onLoadInBackground(Void... arg) {
        TUMRoomFinderRequest requestHandler = new TUMRoomFinderRequest();

        // First search for roomId and mapId
        if (location != null && !location.isEmpty() && roomInfo == null) {
            ArrayList<HashMap<String, String>> request = requestHandler.fetchRooms(location);
            if (request.size() > 0) {
                HashMap<String, String> room = request.get(0);

                roomInfo = new Bundle();
                roomInfo.putString(RoomFinderDetailsActivity.BUILDING_ID, room.get(TUMRoomFinderRequest.KEY_Building + TUMRoomFinderRequest.KEY_ID));
                roomInfo.putString(RoomFinderDetailsActivity.BUILDING_TITLE, room.get(TUMRoomFinderRequest.KEY_Building + TUMRoomFinderRequest.KEY_TITLE));
                roomInfo.putString(RoomFinderDetailsActivity.ROOM_ID, room.get(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER));
                roomInfo.putString(RoomFinderDetailsActivity.CAMPUS_TITLE, room.get(TUMRoomFinderRequest.KEY_Campus + TUMRoomFinderRequest.KEY_TITLE));
                roomInfo.putString(RoomFinderDetailsActivity.ROOM_TITLE, room.get(TUMRoomFinderRequest.KEY_ROOM + TUMRoomFinderRequest.KEY_TITLE));
            }
        }

        if(roomInfo==null) {
            showErrorLayout();
            return null;
        }

        if (mapId.isEmpty()) {
            mapId = requestHandler.fetchDefaultMapId(roomInfo.getString(BUILDING_ID));
        }

        String url = null;
        try {
            String roomId = roomInfo.getString(ROOM_ID);
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

        if (url == null) {
            showErrorLayout();
            return null;
        }
        return net.downloadImageToBitmap(url);
    }

    @Override
    protected void onLoadFinished(Bitmap result) {
        mImage.setImageBitmap(result);
        mDetails1.setText(roomInfo.getString(ROOM_TITLE));
        mDetails2.setText(roomInfo.getString(BUILDING_TITLE));
        showLoadingEnded();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mapsList = request.fetchAvailableMaps(roomInfo.getString(ROOM_ID));
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
