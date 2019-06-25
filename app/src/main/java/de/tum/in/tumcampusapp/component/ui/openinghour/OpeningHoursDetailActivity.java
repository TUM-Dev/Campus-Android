package de.tum.in.tumcampusapp.component.ui.openinghour;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

public class OpeningHoursDetailActivity extends BaseActivity {

    public OpeningHoursDetailActivity() {
        super(R.layout.activity_openinghoursdetails);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            int itemId = getIntent().getIntExtra(OpeningHoursDetailFragment.ARG_ITEM_ID, 0);
            String itemContent = getIntent().getStringExtra(OpeningHoursDetailFragment.ARG_ITEM_CONTENT);
            boolean isTwoPane = getIntent().getBooleanExtra(OpeningHoursDetailFragment.TWO_PANE, false);

            OpeningHoursDetailFragment fragment =
                    OpeningHoursDetailFragment.newInstance(itemId, itemContent, isTwoPane);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    public void openLink(View view) {
        String url = (String) view.getTag();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

}
