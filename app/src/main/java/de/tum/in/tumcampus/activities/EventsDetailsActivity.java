package de.tum.in.tumcampus.activities;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampus.models.managers.EventManager;

/**
 * Activity to show event details (name, location, image, description, etc.)
 */
public class EventsDetailsActivity extends ActionBarActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_eventsdetails);

		// get event details from db
		EventManager em = new EventManager(this);
		Cursor c = em.getDetailsFromDb(getIntent().getStringExtra(
				Const.ID_EXTRA));

		if (!c.moveToNext())
			return;

		String description = c.getString(c
				.getColumnIndex(Const.DESCRIPTION_COLUMN));
		String image = c.getString(c.getColumnIndex(Const.IMAGE_COLUMN));

		String[] weekDays = getString(R.string.week_splitted).split(",");

		setTitle(c.getString(c.getColumnIndex(Const.NAME_COLUMN)));

		/**
		 * <pre>
		 * show infos as:
		 * Week-day, Start DateTime - End Time
		 * Location
		 * Link
		 * </pre>
		 */
		String infos = weekDays[c
				.getInt(c.getColumnIndex(Const.WEEKDAY_COLUMN))] + ", ";
		infos += c.getString(c.getColumnIndex(Const.START_DE_COLUMN)) + " - "
				+ c.getString(c.getColumnIndex(Const.END_DE_COLUMN)) + "\n";

		infos += c.getString(c.getColumnIndex(Const.LOCATION_COLUMN)) + "\n";
		infos += c.getString(c.getColumnIndex(Const.LINK_COLUMN));

		TextView tv = (TextView) findViewById(R.id.infos);
		tv.setText(infos.trim());

		tv = (TextView) findViewById(R.id.description);
		tv.setText(description);

		ImageView iv = (ImageView) findViewById(R.id.image);
		Bitmap b = BitmapFactory.decodeFile(image);
		iv.setImageBitmap(Bitmap.createScaledBitmap(b, 360,
				(b.getHeight() * 360) / b.getWidth(), true));
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.infos);
	}
}