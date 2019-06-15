package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.tumui.lectures.fragment.LecturesFragment;

/**
 * This activity presents the user's lectures. The results can be filtered by the semester.
 * <p>
 * This activity uses the same models as FindLectures.
 * <p>
 * HINT: a TUMOnline access token is needed
 */
public class LecturesPersonalActivity extends BaseActivity {

    public LecturesPersonalActivity() {
        super(R.layout.activity_lectures);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, LecturesFragment.newInstance())
                    .commit();
        }
    }

}
