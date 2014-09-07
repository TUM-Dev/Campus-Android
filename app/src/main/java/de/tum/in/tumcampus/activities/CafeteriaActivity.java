package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.CafeteriaDetailsSectionsPagerAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.managers.CafeteriaManager;

/**
 * Lists all dishes at given cafeteria
 * 
 * @author Sascha Moecker, Haris Iltifat, Thomas Krex
 * 
 */
public class CafeteriaActivity extends ActivityForDownloadingExternal implements ActionBar.OnNavigationListener {

    private ViewPager mViewPager;
    private String mCafeteriaId;
    private MatrixCursor cafeteriaCursor;
    private CafeteriaDetailsSectionsPagerAdapter mSectionsPagerAdapter;

    public CafeteriaActivity() {
        super(Const.CAFETERIAS, R.layout.activity_cafeteria);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get Id and name from intent (calling activity)
        final Intent intent = getIntent();
        if(intent!=null && intent.getExtras()!=null
                && intent.getExtras().containsKey(Const.CAFETERIA_ID))
    		mCafeteriaId = intent.getExtras().getString(Const.CAFETERIA_ID);
        mViewPager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_section_fragment_cafeteria_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.action_ingredients) {
			// Build a alert dialog containing the mapping of ingredients to the numbers
			new AlertDialog.Builder(this).setTitle(R.string.action_ingredients)
			    .setMessage(menuToSpan(this, getResources().getString(R.string.cafeteria_ingredients)))
                .setPositiveButton(android.R.string.ok, null).create().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @SuppressWarnings("deprecation")
    @Override
    protected void onStart() {
        super.onStart();

        CafeteriaManager cafeteriaManager = new CafeteriaManager(this);

        // Get all available cafeterias from database
        Cursor cursor = cafeteriaManager.getAllFromDb("% %");

        int selIndex = -1;
        int i=0;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        cafeteriaCursor = new MatrixCursor(cursor.getColumnNames());
        if (cursor.moveToFirst()) {
            do {
                final String key = cursor.getString(2);
                if (sharedPrefs.getBoolean("mensa_"+key, true) || key.equals(mCafeteriaId)) {
                    if(key.equals(mCafeteriaId)) {
                        selIndex = i;
                    } else if(mCafeteriaId==null && i==0) {
                        mCafeteriaId = key;
                    }
                    cafeteriaCursor.addRow(new Object[]{cursor.getString(0), cursor.getString(1), key});
                    i++;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        this.startManagingCursor(cafeteriaCursor);

        // Iterate over all cafeterias and add them to the listview
        if (cafeteriaCursor.getCount() == 1) {
            // Get Id and name of the database object
            cafeteriaCursor.moveToFirst();
            mCafeteriaId = cafeteriaCursor.getString(cafeteriaCursor.getColumnIndex(Const.ID_COLUMN));
            final String cafeteriaName = cafeteriaCursor.getString(cafeteriaCursor.getColumnIndex(Const.NAME_COLUMN));
            setTitle(cafeteriaName);
        } else if (cafeteriaCursor.getCount() > 0) {
            // Adapter for drop-down navigation
            SimpleCursorAdapter adapterCafeterias = new SimpleCursorAdapter(this, R.layout.simple_spinner_item_actionbar, cafeteriaCursor, cafeteriaCursor.getColumnNames(),
                    new int[] { android.R.id.text1, android.R.id.text2 });
            adapterCafeterias.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_actionbar);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            getSupportActionBar().setListNavigationCallbacks(adapterCafeterias, this);

            // Select item
            if(selIndex>-1)
                getSupportActionBar().setSelectedNavigationItem(selIndex);
        } else {
            // If something went wrong or no cafeterias found
            showErrorLayout();
        }
    }

    @Override
    public boolean onNavigationItemSelected(int pos, long id) {
        mCafeteriaId = "" + id;

        // Create the adapter that will return a fragment for each of the primary sections of the app.
        if (mSectionsPagerAdapter == null) {
            mSectionsPagerAdapter = new CafeteriaDetailsSectionsPagerAdapter(getSupportFragmentManager());
            mSectionsPagerAdapter.setCafeteriaId(this, mCafeteriaId);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        } else {
            mSectionsPagerAdapter.setCafeteriaId(this, mCafeteriaId);
        }
        return true;
    }

    public static SpannableString menuToSpan(Context context, String menu) {
        int len;
        do {
            len = menu.length();
            menu = menu.replaceFirst("\\(([A-Za-z0-9]+),", "($1)(");
        } while (menu.length() > len);
        SpannableString text = new SpannableString(menu);
        replaceWithImg(context, menu, text, "(v)", R.drawable.meal_vegan);
        replaceWithImg(context, menu, text, "(f)", R.drawable.meal_veggie);
        replaceWithImg(context, menu, text, "(R)", R.drawable.meal_beef);
        replaceWithImg(context, menu, text, "(S)", R.drawable.meal_pork);
        replaceWithImg(context, menu, text, "(GQB)", R.drawable.ic_gqb);
        replaceWithImg(context, menu, text, "(99)", R.drawable.meal_alcohol);
        return text;
    }

    private static void replaceWithImg(Context context, String menu, SpannableString text, String sym, int drawable) {
        int ind = menu.indexOf(sym);
        while (ind >= 0) {
            ImageSpan is = new ImageSpan(context, drawable);
            text.setSpan(is, ind, ind + sym.length(), 0);
            ind = menu.indexOf(sym, ind + sym.length());
        }
    }
}
