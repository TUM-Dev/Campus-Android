package de.tum.in.tumcampus.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.CardsAdapter;
import de.tum.in.tumcampus.adapters.SideNavigationAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.SwipeDismissList;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.services.BackgroundService;
import de.tum.in.tumcampus.services.ImportService;
import de.tum.in.tumcampus.services.SilenceService;

/**
 * Main activity displaying the categories and menu items to start each activity (feature)
 * 
 * @author Sascha Moecker
 */
public class StartActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, SwipeDismissList.OnDismissCallback {
	public static final int REQ_CODE_COLOR_CHANGE = 0;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

	/**
	 * Receiver for Services
	 */
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ImportService.BROADCAST_NAME)) {
				String message = intent.getStringExtra(Const.MESSAGE_EXTRA);
				String action = intent.getStringExtra(Const.ACTION_EXTRA);

				if (action.length() != 0) {
					Log.i(this.getClass().getSimpleName(), message);
				}
			}
		}
	};
    private ActionBarDrawerToggle mDrawerToggle;
    private CardsAdapter mAdapter;
    private SwipeDismissList mSwipeList;

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check if there is a result key in an intent
		if (data != null && data.hasExtra(Const.PREFS_HAVE_CHANGED) && data.getBooleanExtra(Const.PREFS_HAVE_CHANGED, false)) {
			// Restart the Activity if prefs have changed
			Intent intent = this.getIntent();
			this.finish();
			this.startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_start);

        setTitle(getString(R.string.campus_app));

        // Workaround for new API version. There was a security update which disallows applications to execute HTTP request in the GUI main thread.
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Currently causes problems when dismissing cards, and there
        // is no card that really profits from updating
        // Set up the SwipeRefreshLayout
        /*mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);*/

		// Set up the ViewPager with the sections adapter.
        ListView mCardsView = (ListView) findViewById(R.id.cards_view);
        mAdapter = new CardsAdapter(this);
        mCardsView.setAdapter(mAdapter);
        mCardsView.setOnItemClickListener(this);
        mCardsView.setDividerHeight(0);
        mSwipeList = new SwipeDismissList(mCardsView, this, SwipeDismissList.UndoMode.MULTI_UNDO);
        mSwipeList.setUndoString(getString(R.string.card_dismissed));
        mSwipeList.setUndoMultipleString(getString(R.string.cards_dismissed));

		// Registers receiver for download and import
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ImportService.BROADCAST_NAME);
		this.registerReceiver(this.receiver, intentFilter);

		// Imports default values into database
		Intent service;
		service = new Intent(this, ImportService.class);
		service.putExtra(Const.ACTION_EXTRA, Const.DEFAULTS);
		this.startService(service);

		// Start silence Service (if already started it will just invoke a check)
		service = new Intent(this, SilenceService.class);
		this.startService(service);

		// Start daily Service (same here: if already started it will just invoke a check)
		service = new Intent(this, BackgroundService.class);
		this.startService(service);

		// Setup the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new SideNavigationAdapter(this));

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.menu_start_activity, menu);
		return true;
	}

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		// important to unregister the broadcast receiver
		this.unregisterReceiver(this.receiver);
	}

    // Discard all pending discards, otherwise already discarded item will show up again
    @Override
    protected void onStop() {
        super.onStop();
        mSwipeList.discardUndo();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		switch (item.getItemId()) {
		case R.id.action_settings:
			// Opens the preferences screen
			Intent intent = new Intent(this, UserPreferencesActivity.class);
			this.startActivityForResult(intent, REQ_CODE_COLOR_CHANGE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (adapterView.getId()) {
            case R.id.left_drawer:
                SideNavigationAdapter.SideNavigationItem sideNavigationItem = (SideNavigationAdapter.SideNavigationItem) adapterView.getAdapter().getItem(position);
                if(sideNavigationItem.getActivity()==null)
                    break;
                Intent newActivity = new Intent(this.getApplicationContext(), sideNavigationItem.getActivity());
                this.startActivity(newActivity);
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case R.id.cards_view:
                Card card = CardManager.getCard(position);
                if(card.getTyp()==CardManager.CARD_RESTORE) {
                    CardManager.restoreCards();
                    mSwipeList.cancelUndo();
                    mAdapter.notifyDataSetChanged();
                } else {
                    Intent i = card.getIntent();
                    if(i!=null) {
                        startActivity(i);
                    }
                }
                break;
        }
    }

    /**
     * Handle refresh request
     * */
    /*@Override
    public void onRefresh() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CardManager.update(StartActivity.this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mSwipeLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }*/

    /**
    * Handle swipe to dismiss events
    * */
    @Override
    public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {
        // Delete the item from adapter
        final Card itemToDelete = mAdapter.remove(position);
        return new SwipeDismissList.Undoable() {
            @Override
            public void undo() {
                // Return the item at its previous position again
                mAdapter.insert(position, itemToDelete);
            }

            @Override
            public void discard() {
                itemToDelete.discardCard();
            }
        };
    }
}
