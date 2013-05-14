package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.Feed;
import de.tum.in.tumcampusapp.models.managers.FeedManager;

public class FeedsActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {
	private static String feedId;
	private static String feedName;
	private SimpleCursorAdapter adapter;

	@Override
	public void onClick(View v) {
		// add a new feed
		EditText editName = (EditText) findViewById(R.id.name);
		EditText editUrl = (EditText) findViewById(R.id.url);

		String name = editName.getText().toString();
		String url = editUrl.getText().toString();

		// prepend http:// if needed
		if (url.length() > 0 && !url.contains(":")) {
			url = "http://" + url;
		}
		FeedManager fm = new FeedManager(this);
		try {
			Feed feed = new Feed(name, url);
			fm.insertUpdateIntoDb(feed);
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		// refresh feed list
		adapter.changeCursor(fm.getAllFromDb());

		// clear form
		editName.setText("");
		editUrl.setText("");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feeds);

		FeedManager fm = new FeedManager(this);
		Cursor c = fm.getAllFromDb();

		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, c.getColumnNames(), new int[] { android.R.id.text1 });

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		Button save = (Button) findViewById(R.id.save);
		save.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		// click on feed in list
		if (position != -1) {
			Cursor c = (Cursor) av.getAdapter().getItem(position);
			feedId = c.getString(c.getColumnIndex(Const.ID_COLUMN));
			feedName = c.getString(c.getColumnIndex(Const.NAME_COLUMN));
		}

		Intent intent = new Intent(this, FeedsDetailsActivity.class);
		intent.putExtra(Const.FEED_ID, feedId);
		intent.putExtra(Const.FEED_NAME, feedName);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> av, View v, final int position, long id) {
		if (id == -1) {
			return false;
		}

		// confirm delete
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int id) {

				// delete feed from list, refresh feed list
				Cursor c = (Cursor) av.getAdapter().getItem(position);
				int _id = c.getInt(c.getColumnIndex(Const.ID_COLUMN));

				FeedManager fm = new FeedManager(av.getContext());
				fm.deleteFromDb(_id);
				adapter.changeCursor(fm.getAllFromDb());
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.really_delete));
		builder.setPositiveButton(getString(R.string.yes), listener);
		builder.setNegativeButton(getString(R.string.no), null);
		builder.show();
		return false;
	}
}