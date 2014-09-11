package de.tum.in.tumcampus.activities;

import android.graphics.Bitmap;
import android.os.Bundle;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.Utils;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Displays the map regarding the searched room.
 * 
 */
public class RoomFinderDetailsActivity extends ActivityForLoadingInBackground<String,Bitmap> {

    private ImageViewTouch mImage;

    public RoomFinderDetailsActivity() {
        super(R.layout.activity_roomfinderdetails);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mImage = (ImageViewTouch) findViewById(R.id.activity_roomfinder_details);
        mImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

		String roomId = getIntent().getExtras().getString("roomId");
		String mapId = getIntent().getExtras().getString("mapId");

        startLoading("http://vmbaumgarten3.informatik.tu-muenchen.de/roommaps/room/map?id=" + roomId + "&mapid=" + mapId);
	}

    @Override
    protected Bitmap onLoadInBackground(String... arg) {
        return Utils.downloadImageAndCompressThread(arg[0]);
    }

    @Override
    protected void onLoadFinished(Bitmap result) {
        mImage.setImageBitmap(result);
    }
}
