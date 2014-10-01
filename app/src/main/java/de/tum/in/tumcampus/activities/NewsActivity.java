package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.NewsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.NewsManager;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends ActivityForDownloadingExternal implements OnItemClickListener {

    private ListView lv;
    private Parcelable state;

    public NewsActivity() {
		super(Const.NEWS, R.layout.activity_news);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestDownload(false);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Gets all news from database
		NewsManager nm = new NewsManager(this);
		Cursor cursor = nm.getAllFromDb();
		if (cursor.getCount() > 0) {
			NewsAdapter adapter = new NewsAdapter(this, cursor);

			lv = (ListView) findViewById(R.id.activity_news_list_view);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(this);
            lv.setDividerHeight(0);
            lv.setSelection(nm.getTodayIndex());

            /** Restore previous state (including selected item index and scroll position) */
            if(state!=null)
                lv.onRestoreInstanceState(state);
		} else {
			showErrorLayout();
		}
	}

    /**
     * If news item has been clicked open the corresponding link
     * @param adapterView Containing listView
     * @param view Item view
     * @param position Index of the item
     * @param id Item id
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Cursor cursor = (Cursor) lv.getAdapter().getItem(position);
        String url = cursor.getString(cursor.getColumnIndex(Const.LINK_COLUMN));
        if (url.length() == 0) {
            Utils.showToast(this, R.string.no_link_existing);
            return;
        }

        // Opens url in browser
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onRefreshStarted(View view) {
        requestDownload(true);
    }

    /** Save ListView state */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        state = lv.onSaveInstanceState();
    }
}