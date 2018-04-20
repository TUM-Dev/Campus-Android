package de.tum.in.tumcampusapp.component.ui.plan;

import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

/**
 * Activity to show plan details.
 */
public class PlansDetailsActivity extends BaseActivity {

    public static final String PLAN_TITLE_ID = "plan_title_id";
    public static final String PLAN_IMG_ID = "plan_img_id";

    public PlansDetailsActivity() {
        super(R.layout.activity_zoomable_image);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title = getIntent().getExtras()
                               .getInt(PLAN_TITLE_ID);
        int img = getIntent().getExtras()
                             .getInt(PLAN_IMG_ID);

        PhotoView imageViewTouch = findViewById(R.id.zoomable_image);
        imageViewTouch.setImageResource(img);
        imageViewTouch.setMaximumScale(10);

        setTitle(getString(title));
    }
}