package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.adapters.CardsAdapter;
import de.tum.in.tumcampus.adapters.NavigationDrawerAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.SwipeDismissList;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.services.SilenceService;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Main activity displaying the categories and menu items to start each activity (feature)
 * 
 * @author Sascha Moecker
 */
public class StartActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, SwipeDismissList.OnDismissCallback, OnRefreshListener {
    private static final int MENU_OPEN_SETTINGS = 0;
    private static final int MENU_HIDE_ALWAYS = 1;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CardsAdapter mAdapter;
    private SwipeDismissList mSwipeList;
    private PullToRefreshLayout mPullToRefreshLayout;
    private ListView mCardsView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0,0);
        //overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(R.layout.activity_start);

        setTitle(getString(R.string.campus_app));

        // Setup card list view
        mCardsView = (ListView) findViewById(R.id.cards_view);
        mCardsView.setOnItemClickListener(this);
        mCardsView.setDividerHeight(0);
        mAdapter = new CardsAdapter(this);
        mCardsView.setAdapter(mAdapter);
        registerForContextMenu(mCardsView);

        // Setup swipe to dismiss feature
        mSwipeList = new SwipeDismissList(mCardsView, this, SwipeDismissList.UndoMode.MULTI_UNDO);
        mSwipeList.setUndoString(getString(R.string.card_dismissed));
        mSwipeList.setUndoMultipleString(getString(R.string.cards_dismissed));

        // Setup pull to refresh
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this).allChildrenArePullable()
                .listener(this).setup(mPullToRefreshLayout);

		// Start silence Service (if already started it will just invoke a check)
        Intent service = new Intent(this, SilenceService.class);
		this.startService(service);

		// Setup the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new NavigationDrawerAdapter(this));

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
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (adapterView.getId()) {
            case R.id.left_drawer:
                NavigationDrawerAdapter.SideNavigationItem sideNavigationItem = (NavigationDrawerAdapter.SideNavigationItem) adapterView.getAdapter().getItem(position);
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
                    refreshCards();
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

    /**
     * Handle long click events on a card
     * */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.options);
        menu.add(Menu.NONE, MENU_OPEN_SETTINGS, Menu.NONE, R.string.open_card_settings);
        menu.add(Menu.NONE, MENU_HIDE_ALWAYS, Menu.NONE, R.string.always_hide_card);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Card card = (Card) mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case MENU_OPEN_SETTINGS:
                // Open card's preference screen
                String key = card.getSettings();
                if(key==null)
                    return true;
                Intent intent = new Intent(this, UserPreferencesActivity.class);
                intent.putExtra(Const.PREFERENCE_SCREEN, key);
                startActivity(intent);
                return true;
            case MENU_HIDE_ALWAYS:
                card.hideAlways();
                refreshCards();
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void refreshCards() {
        mPullToRefreshLayout.setRefreshing(true);
        onRefreshStarted(mCardsView);
    }
    @Override
    public void onRefreshStarted(View view) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeList.cancelUndo();
            }

            @Override
            protected Void doInBackground(Void... params) {
                CardManager.update(StartActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                mAdapter.notifyDataSetChanged();
                mPullToRefreshLayout.setRefreshComplete();
            }
        }.execute();
    }
}
