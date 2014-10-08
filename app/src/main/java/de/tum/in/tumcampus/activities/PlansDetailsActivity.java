package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Activity to show plan details.
 */
public class PlansDetailsActivity extends ActionBarActivity {

    public static final String PLAN_TITLE_ID = "plan_title_id";
    public static final String PLAN_IMG_ID = "plan_img_id";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
		setContentView(R.layout.activity_plans_details);

        int title = getIntent().getExtras().getInt(PLAN_TITLE_ID);
        int img = getIntent().getExtras().getInt(PLAN_IMG_ID);

		ImageViewTouch imageViewTouch = (ImageViewTouch) findViewById(R.id.activity_plans_plan);
        imageViewTouch.setImageResource(img);
        imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        imageViewTouch.setDoubleTapEnabled(false);

	    setTitle(getString(title));
	}
}