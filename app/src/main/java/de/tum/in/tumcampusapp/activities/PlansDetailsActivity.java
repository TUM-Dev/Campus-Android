package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.ImplicitCounter;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Activity to show plan details.
 */
public class PlansDetailsActivity extends AppCompatActivity {

    public static final String PLAN_TITLE_ID = "plan_title_id";
    public static final String PLAN_IMG_ID = "plan_img_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.count(this);
        setContentView(R.layout.activity_plans_details);

        int title = getIntent().getExtras()
                               .getInt(PLAN_TITLE_ID);
        int img = getIntent().getExtras()
                             .getInt(PLAN_IMG_ID);

        ImageView imageViewTouch = findViewById(R.id.activity_plans_plan);
        imageViewTouch.setImageResource(img);

        //Attach touch handler to imageView
        new PhotoViewAttacher(imageViewTouch);

        setTitle(getString(title));
    }
}