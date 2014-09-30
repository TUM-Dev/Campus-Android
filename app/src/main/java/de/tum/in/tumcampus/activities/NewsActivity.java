package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.NewsManager;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends ActivityForDownloadingExternal implements OnItemClickListener, ViewBinder {

	public NewsActivity() {
		super(Const.NEWS, R.layout.activity_news);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestDownload(false);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();

		// Gets all news from database
		NewsManager nm = new NewsManager(this);
		Cursor cursor = nm.getAllFromDb();
		startManagingCursor(cursor);
		if (cursor.getCount() > 0) {
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.card_news_item, cursor,
					cursor.getColumnNames(), new int[] { R.id.news_img, R.id.news_title,
                    R.id.news_src_date, R.id.news_src_title, R.id.news_src_icon });

			adapter.setViewBinder(this);

			ListView lv = (ListView) findViewById(R.id.activity_news_list_view);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(this);
            lv.setDividerHeight(0);

			// Resets new items counter
			NewsManager.lastInserted = 0;
		} else {
			showErrorLayout();
		}
	}

    /**
     * Correctly bind cursor data to views
     * @param view Item view
     * @param cursor Cursor containing the data
     * @param index index of the cursor column to be set
     * @return True if bind was handled
     */
	@Override
	public boolean setViewValue(View view, Cursor cursor, int index) {
        if (view.getId() == R.id.news_img) {
            ImageView img = (ImageView) view;
            String imgUrl = cursor.getString(index);
            if(imgUrl.isEmpty()) {
                img.setVisibility(View.GONE);
            } else {
                img.setVisibility(View.VISIBLE);
                Utils.loadAndSetImage(this, imgUrl, img);
            }
            return true;
        }

        if (view.getId() == R.id.news_title) {
            String title = cursor.getString(index);
            if(title.contains("\n")) {
                title = title.substring(0, title.indexOf('\n'));
            }
            ((TextView) view).setText(title);
            return true;
        }

		// Adds url (domain only) to date
		if (view.getId() == R.id.news_src_date) {
			String date = cursor.getString(index);
            ((TextView) view).setText(date);
            return true;
		}

        if(view.getId() == R.id.news_src_title) {
            String link = cursor.getString(cursor.getColumnIndex(Const.LINK_COLUMN));

            if (link.length() > 0) {
                TextView tv = (TextView) view;
                if(Uri.parse(link).getHost().equals("graph.facebook.com")) {
                    tv.setText("Facebook");
                } else {
                    tv.setText(Uri.parse(link).getHost());
                }
                return true;
            }
        }

        if(view.getId() == R.id.news_src_icon) {
            String link = cursor.getString(cursor.getColumnIndex(Const.LINK_COLUMN));

            if (link.length() > 0) {
                ImageView img = (ImageView) view;
                if(Uri.parse(link).getHost().equals("graph.facebook.com")) {
                    img.setImageResource(R.drawable.ic_facebook);
                } else {
                    img.setImageResource(R.drawable.ic_comment);
                }
                return true;
            }
        }

		// hide empty view elements
		if (cursor.getString(index).length() == 0) {
			view.setVisibility(View.GONE);

			// no binding needed
			return true;
		}
		view.setVisibility(View.VISIBLE);
		return false;
	}

    /**
     * If news item has been clicked open the corresponding link
     * @param adapterView Containing listView
     * @param view Item view
     * @param position Index of the item
     * @param id Item id
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ListView lv = (ListView) findViewById(R.id.activity_news_list_view);

        Cursor cursor = (Cursor) lv.getAdapter().getItem(position);
        startManagingCursor(cursor);

        String url = cursor.getString(cursor.getColumnIndex(Const.LINK_COLUMN));

        if (url.length() == 0) {
            Utils.showToast(this, R.string.no_link_existing);
            return;
        }

        // Opens url in browser
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(viewIntent);
    }

    @Override
    public void onRefreshStarted(View view) {
        requestDownload(true);
    }
}