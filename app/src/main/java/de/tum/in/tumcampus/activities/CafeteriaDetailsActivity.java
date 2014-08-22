package de.tum.in.tumcampus.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.CafeteriaDetailsSectionsPagerAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.cards.CafeteriaMenuCard;

/**
 * Lists all dishes at given cafeteria
 * 
 * @author Sascha Moecker, Haris Iltifat, Thomas Krex
 * 
 */
public class CafeteriaDetailsActivity extends ActionBarActivity {

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cafeteriadetails);

		// Get Id and name from intent (calling activity)
		String cafeteriaId = getIntent().getExtras().getString(Const.CAFETERIA_ID);
		String cafeteriaName = getIntent().getExtras().getString(Const.CAFETERIA_NAME);

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
        CafeteriaDetailsSectionsPagerAdapter mSectionsPagerAdapter = new CafeteriaDetailsSectionsPagerAdapter(this,
                getSupportFragmentManager(), cafeteriaId, cafeteriaName);

		// Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(
				R.menu.menu_section_fragment_cafeteria_details, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_ingredients:
			// Build a alert dialog containing the mapping of ingredients to the
			// numbers
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(R.string.action_ingredients);
			alertDialog.setMessage(CafeteriaMenuCard.menuToSpan(this,getResources().getString(
					R.string.cafeteria_ingredients)));
			alertDialog.setButton(
					getResources().getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// here you can add functions
						}
					});
			alertDialog.setIcon(android.R.drawable.ic_menu_info_details);
			alertDialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
