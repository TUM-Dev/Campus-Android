package de.tum.in.tumcampusapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.LectureItemManager;
import de.tum.in.tumcampusapp.models.managers.LectureManager;

/**
 * Activity to show lectures and lecture units
 */
public class LecturesActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, ViewBinder, OnClickListener {

	/**
	 * Current lecture selected
	 */
	String lectureId;

	/**
	 * Deletes a lecture and refreshes both list views
	 * 
	 * <pre>
	 * @param itemId Lecture id
	 * </pre>
	 */
	public void deleteLecture(String itemId) {
		// delete lecture
		LectureManager lm = new LectureManager(this);
		lm.deleteItemFromDb(itemId);

		// refresh lecture list
		ListView lv2 = (ListView) findViewById(R.id.listView2);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv2.getAdapter();
		adapter.changeCursor(lm.getAllFromDb());

		// delete lecture items
		LectureItemManager lim = new LectureItemManager(this);
		lim.deleteLectureFromDb(itemId);

		// refresh lecture unit list if viewing deleted lecture or
		// recent lectures (could contain a unit from deleted lecture)
		ListView lv = (ListView) findViewById(R.id.listView);
		adapter = (SimpleCursorAdapter) lv.getAdapter();

		if (lectureId == null || lectureId.equals(itemId)) {
			adapter.changeCursor(lim.getRecentFromDb());

			TextView tv = (TextView) findViewById(R.id.lectureText);
			tv.setText(getString(R.string.next_lectures));
			TextView tv2 = (TextView) findViewById(R.id.moduleText);
			tv2.setText("");

			// unselect current lecture (no longer exists)
			lectureId = null;
		}
	}

	/**
	 * Deletes a lecture unit and refreshes the lecture unit list
	 * 
	 * <pre>
	 * @param itemId Lecture unit id
	 * </pre>
	 */

	public void deleteLectureItem(String itemId) {
		// delete lecture item
		LectureItemManager lim = new LectureItemManager(this);
		lim.deleteItemFromDb(itemId);

		ListView lv = (ListView) findViewById(R.id.listView);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
		if (lectureId == null) {
			adapter.changeCursor(lim.getRecentFromDb());
		} else {
			adapter.changeCursor(lim.getAllFromDb(lectureId));
		}
	}

	/**
	 * @author Florian Schulz
	 * @soves SlideBar
	 */
	// TODO Review Vasyl
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.slide_my_lectures) {
			Intent iMyLectures = new Intent(this.getBaseContext(), LecturesPersonalActivity.class);
			startActivity(iMyLectures);
		}
		if (v.getId() == R.id.slide_search_lectures) {
			Intent iFindLectures = new Intent(this.getBaseContext(), LecturesSearchActivity.class);
			startActivity(iFindLectures);
		}
		return;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lectures);

		// get all upcoming lecture units
		LectureItemManager lim = new LectureItemManager(this);
		Cursor c = lim.getRecentFromDb();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, c, c.getColumnNames(), new int[] { android.R.id.text1,
				android.R.id.text2 });

		adapter.setViewBinder(this);

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		// get all lectures
		LectureManager lm = new LectureManager(this);
		Cursor c2 = lm.getAllFromDb();

		SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c2, c2.getColumnNames(),
				new int[] { android.R.id.text1 });

		adapter2.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor c, int index) {
				// truncate lecture names to 20 characters
				String name = c.getString(index);
				TextView tv = (TextView) view;
				tv.setText(Utils.trunc(name, 20));
				return true;
			}
		});

		ListView lv2 = (ListView) findViewById(R.id.listView2);
		lv2.setAdapter(adapter2);
		lv2.setOnItemClickListener(this);
		lv2.setOnItemLongClickListener(this);

		/**
		 * @author Florian Schulz
		 * @soves SlideBar
		 */
		Button bv1 = (Button) findViewById(R.id.slide_my_lectures);
		Button bv2 = (Button) findViewById(R.id.slide_search_lectures);
		// Button bv3 = (Button) findViewById(R.id.slide_calendarexport);
		bv1.setOnClickListener(this);
		bv2.setOnClickListener(this);
		// bv3.setOnClickListener(this);
		// reset new items counter
		LectureItemManager.lastInserted = 0;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {

		ListView lv = (ListView) findViewById(R.id.listView);
		ListView lv2 = (ListView) findViewById(R.id.listView2);

		// Click on lecture list
		if (av.getId() == R.id.listView2) {
			Cursor c2 = (Cursor) lv2.getAdapter().getItem(position);
			lectureId = c2.getString(c2.getColumnIndex(Const.ID_COLUMN));
			String name = c2.getString(c2.getColumnIndex(Const.NAME_COLUMN));
			String module = c2.getString(c2.getColumnIndex(Const.MODULE_COLUMN));

			// get all lecture units from a lecture
			LectureItemManager lim = new LectureItemManager(this);

			SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv.getAdapter();
			adapter.changeCursor(lim.getAllFromDb(lectureId));

			TextView tv = (TextView) findViewById(R.id.lectureText);
			tv.setText(Utils.trunc(name + ":", 35));

			// Link to lecture module homepage (e.g. contains ECTS)
			String moduleUrl = "https://drehscheibe.in.tum.de/myintum/kurs_verwaltung/cm.html.de?id=" + module;

			TextView tv2 = (TextView) findViewById(R.id.moduleText);
			tv2.setText(Html.fromHtml("<a href='" + moduleUrl + "'>" + module + "</a>"));
			tv2.setMovementMethod(LinkMovementMethod.getInstance());
			return;
		}

		// click on lecture unit list
		Cursor c = (Cursor) lv.getAdapter().getItem(position);
		String url = c.getString(c.getColumnIndex(Const.URL_COLUMN));

		// empty link => no action
		if (url.equals("about:blank")) {
			return;
		}

		// tumonline search page => more lecture details
		// 1595 = WS2012/13
		// TODO make it flexible
		if (url.length() == 0) {
			url = "https://campus.tum.de/tumonline/wbSuche.LVSucheSimple?" + "pLVNrFlag=J&pSjNr=1595&pSemester=A&pSuchbegriff="
					+ c.getString(c.getColumnIndex("lectureId"));
		}

		// Connection to browser
		Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(viewIntent);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> av, View v, final int position, long id) {

		// confirm deleting lectures or lecture units
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				Cursor c = (Cursor) av.getAdapter().getItem(position);
				String itemId = c.getString(c.getColumnIndex(Const.ID_COLUMN));

				if (av.getId() == R.id.listView) {
					deleteLectureItem(itemId);
				} else {
					deleteLecture(itemId);
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.really_delete));
		builder.setPositiveButton(getString(R.string.yes), listener);
		builder.setNegativeButton(getString(R.string.no), null);
		builder.show();
		return false;
	}

	/**
	 * @author Florian Schulz
	 * @solves No Hardware-Button Menu used (Style-Reasons) => Slidebar
	 * 
	 * 
	 @Override public boolean onCreateOptionsMenu(Menu menu) {
	 *           super.onCreateOptionsMenu(menu);
	 * 
	 ** 
	 *           Suche für Lehrveranstaltungen in TUMOnline hinzugefügt Und
	 *           Ausgabe Eigene Lehrveranstaltungen angefügt
	 * 
	 * @author Daniel Mayr
	 * 
	 * 
	 *         MenuItem miVorlesungsExport = menu.add(0, Menu.FIRST, 0,
	 *         getString(R.string.export2calendar));
	 *         miVorlesungsExport.setIcon(android
	 *         .R.drawable.ic_menu_my_calendar);
	 * 
	 *         MenuItem miVorlesungssuche = menu.add(1, Menu.FIRST + 1, 0,
	 *         getString(R.string.search_lectures));
	 *         miVorlesungssuche.setIcon(android.R.drawable.ic_menu_search);
	 * 
	 *         MenuItem miMyLectures = menu.add(2, Menu.FIRST + 2, 0,
	 *         getString(R.string.my_lectures));
	 *         miMyLectures.setIcon(android.R.drawable.ic_menu_agenda);
	 * 
	 *         return true; }
	 * 
	 ** 
	 *         kompatiblitaet zu mehreren Menu Punkten hinzugefuegt
	 * 
	 * @author Daniel Mayr
	 * @review Florian Schulz, BAD_PRACTICE fixed (String comparison) Review für
	 *         Vasyl Stringvergleich mit ==
	 * 
	 @Override public boolean onOptionsItemSelected(MenuItem item) {
	 * 
	 *           // find lectures via TUMOnline if
	 *           (item.getTitle().equals(getString(R.string.search_lectures))) {
	 *           onSearchRequested(); }
	 * 
	 *           // show my lectures from TUMOnline if
	 *           (item.getTitle().equals(getString(R.string.my_lectures))) {
	 *           Intent iMyLectures = new Intent(this.getBaseContext(),
	 *           MyLectures.class); startActivity(iMyLectures); }
	 * 
	 *           // export lectures to google calendar if
	 *           (item.getTitle().equals(getString(R.string.export2calendar))) {
	 *           Intent iLectures2Calendar = new Intent(this.getBaseContext(),
	 *           Lectures2Calendar.class); startActivity(iLectures2Calendar); }
	 * 
	 *           return true; }
	 * 
	 * 
	 ** 
	 *           set to use the find hardware button to get to the findlectures
	 *           activity
	 * 
	 * @author Daniel Mayr
	 * 
	 @Override public boolean onSearchRequested() {
	 * 
	 *           Intent iFindLectures = new Intent(this.getBaseContext(),
	 *           FindLectures.class); startActivity(iFindLectures); return
	 *           false; // don't go ahead and show the search box }
	 */

	/**
	 * change presentation of lecture units in the list
	 */
	@Override
	public boolean setViewValue(View view, Cursor c, int index) {
		String[] weekDays = getString(R.string.week_splitted).split(",");

		if (view.getId() == android.R.id.text1) {
			// truncate lecture name to 20 characters,
			// append lecture unit note
			String name = c.getString(c.getColumnIndex(Const.NAME_COLUMN));
			String note = c.getString(c.getColumnIndex(Const.NOTE_COLUMN));
			if (note.length() > 0) {
				note = " - " + note;
			}
			TextView tv = (TextView) view;
			tv.setText(Utils.trunc(name, 20) + note);
			return true;
		}
		if (view.getId() == android.R.id.text2) {
			/**
			 * <pre>
			 * show info as:
			 * Lecture: Week-day, Start DateTime - End Time, Room-Nr-Intern
			 * Holiday: Week-day, Start Date
			 * vacation info: Start Date - End Date
			 * 
			 * Location format: Room-Nr-Intern, Room-name (Room-Nr-Extern)
			 * </pre>
			 */
			String info = "";
			String lectureId = c.getString(c.getColumnIndex(Const.LECTURE_ID_COLUMN));
			// TODO IMPORTANT Check whether "start_dt" and "start_de" are
			// actually the same
			if (lectureId.equals(Const.VACATION)) {
				info = c.getString(c.getColumnIndex(Const.START_DT_COLUMN)) + " - " // TODO
																					// REVIEW
																					// Vasyl
																					// changed
																					// start
																					// to
																					// end
						+ c.getString(c.getColumnIndex(Const.END_DT_COLUMN));

			} else if (lectureId.equals(Const.HOLIDAY)) {
				info = weekDays[c.getInt(c.getColumnIndex(Const.WEEKDAY_COLUMN))] + ", " + c.getString(c.getColumnIndex(Const.START_DT_COLUMN));

			} else {
				info = weekDays[c.getInt(c.getColumnIndex(Const.WEEKDAY_COLUMN))] + ", " + c.getString(c.getColumnIndex(Const.START_DE_COLUMN)) + " - " // TODO
																																						// REVIEW
																																						// Vasyl
																																						// changed
																																						// start
																																						// to
																																						// end
						+ c.getString(c.getColumnIndex(Const.END_DE_COLUMN));

				String location = c.getString(c.getColumnIndex(Const.LOCATION_COLUMN));
				if (location.indexOf(",") != -1) {
					location = location.substring(0, location.indexOf(","));
				}
				if (location.length() != 0) {
					info += ", " + location;
				}
			}
			TextView tv = (TextView) view;
			tv.setText(info);
			return true;
		}
		return false;
	}

}