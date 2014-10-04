package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
public class RoomFinderDetailsActivity extends ActivityForLoadingInBackground<String, Bitmap> implements  DialogInterface.OnClickListener {

    private ImageViewTouch mImage;
    private String mapId;
    private String roomId;
    private boolean mapsLoaded = false;
    private TUMRoomFinderRequest request;
    private ArrayList<HashMap<String, String>> mapsList;
    private NetUtils net;

    public RoomFinderDetailsActivity() {
        super(R.layout.activity_roomfinderdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        mImage = (ImageViewTouch) findViewById(R.id.activity_roomfinder_details);
        mImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        roomId = getIntent().getExtras().getString("roomId");
        mapId = getIntent().getExtras().getString("mapId");
        request = new TUMRoomFinderRequest();

        try {
            if (mapId.equals("10")) {
                startLoading("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/building/defaultMap?id="
                        + URLEncoder.encode(roomId.substring(roomId.indexOf('@') + 1), "UTF-8"));
            } else {
                startLoading("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/map?id="
                        + URLEncoder.encode(roomId, "UTF-8") + "&mapid="
                        + URLEncoder.encode(mapId, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            Utils.log(e);
        }
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
            if(mapsList.get(i).get(TUMRoomFinderRequest.KEY_ID).equals(mapId)) {
                curPos=i;
            }
        }
        new AlertDialog.Builder(this).setSingleChoiceItems(list, curPos, this).show();
    }

    public void onClick(DialogInterface dialog, int whichButton) {
        dialog.dismiss();
        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        mapId = mapsList.get(selectedPosition).get(TUMRoomFinderRequest.KEY_ID);
        try {
            startLoading("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/map?id="
                    + URLEncoder.encode(roomId, "UTF-8") + "&mapid="
                    + URLEncoder.encode(mapId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Utils.log(e);
        }
    }

    private void getDirections() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Geo geo = request.fetchCoordinates(roomId);
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
    protected Bitmap onLoadInBackground(String... arg) {
        return net.downloadImageToBitmap(arg[0]);
    }

    @Override
    protected void onLoadFinished(Bitmap result) {
        mImage.setImageBitmap(result);
        showLoadingEnded();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mapsList = request.fetchAvailableMaps(roomId);
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
