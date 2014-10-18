package de.tum.in.tumcampus.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.BaseActivity;
import de.tum.in.tumcampus.adapters.CardsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.SwipeDismissList;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.services.SilenceService;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Main activity displaying the cards and providing navigation with navigation drawer
 */
public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, SwipeDismissList.OnDismissCallback, OnRefreshListener {
    private static final int MENU_OPEN_SETTINGS = 0;
    private static final int MENU_HIDE_ALWAYS = 1;

    /**
     * Navigation Drawer
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean registered;

    /**
     * Card list
     */
    private ListView mCardsView;
    private CardsAdapter mAdapter;
    private SwipeDismissList mSwipeList;
    private PullToRefreshLayout mPullToRefreshLayout;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup card list view
        mCardsView = (ListView) findViewById(R.id.cards_view);
        mCardsView.setOnItemClickListener(this);
        mCardsView.setDividerHeight(0);
        registerForContextMenu(mCardsView);

        // Setup swipe to dismiss feature
        mSwipeList = new SwipeDismissList(mCardsView, this);
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

        // Set the list's click listener
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

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
        this.getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CardManager.shouldRefresh || CardManager.getCards() == null) {
            refreshCards();
        } else {
            initAdapter();
        }
    }

    /**
     * Setup cards adapter
     */
    private void initAdapter() {
        mAdapter = new CardsAdapter(this);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mCardsView);
        mCardsView.setAdapter(animationAdapter);
    }

    /**
     * Discard all pending discards, otherwise already discarded item will show up again
     */
    @Override
    protected void onStop() {
        super.onStop();
        mSwipeList.discardUndo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registered) {
            unregisterReceiver(connectivityChangeReceiver);
            registered = false;
        }
    }

    /**
     * If drawer is expanded hide settings icon
     *
     * @param menu Menu instance
     * @return True if handled
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Sync the toggle state after onRestoreInstanceState has occurred.
     *
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Let the drawer toggle handle configuration changes
     *
     * @param newConfig The new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Handle expansion of navigation drawer and settings menu item click
     *
     * @param item Clicked menu item
     * @return True if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Opens the preferences screen
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, UserPreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle on card click
     *
     * @param adapterView Containing listView
     * @param view        Item view
     * @param position    Index of item
     * @param id          Item id
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Card card = CardManager.getCard(position);
        if (card.getTyp() == CardManager.CARD_RESTORE) {
            mSwipeList.discardUndo();
            CardManager.restoreCards();
            refreshCards();
        } else {
            Intent i = card.getIntent();
            if (i != null) {
                startActivity(i);
            }
        }
    }

    public void onFabClicked(View v) {
        startActivity(new Intent(this, UserPreferencesActivity.class));
    }

    /**
     * Handle swipe to dismiss events
     *
     * @param listView ListView
     * @param position Swiped item position
     */
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
     *
     * @param menu     Context menu
     * @param v        Item view
     * @param menuInfo info
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Card card = (Card) mAdapter.getItem(info.position);
        String key = card.getSettings();
        if (key == null) {
            return;
        }
        menu.setHeaderTitle(R.string.options);
        menu.add(Menu.NONE, MENU_OPEN_SETTINGS, Menu.NONE, R.string.open_card_settings);
        menu.add(Menu.NONE, MENU_HIDE_ALWAYS, Menu.NONE, R.string.always_hide_card);
    }

    /**
     * Handle context menu item events
     *
     * @param item Menu item
     * @return True if handled
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Card card = (Card) mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case MENU_OPEN_SETTINGS:
                // Open card's preference screen
                String key = card.getSettings();
                if (key == null)
                    return true;
                Intent intent = new Intent(this, UserPreferencesActivity.class);
                intent.putExtra(Const.PREFERENCE_SCREEN, key);
                intent.putExtra("returnHome", true);
                startActivity(intent);
                return true;
            case MENU_HIDE_ALWAYS:
                card.hideAlways();
                refreshCards();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Show progress indicator and start updating cards in background
     */
    private void refreshCards() {
        mPullToRefreshLayout.setRefreshing(true);
        onRefreshStarted(mCardsView);
    }

    /**
     * Starts updating cards in background
     * Called when {@link PullToRefreshLayout} gets triggered.
     *
     * @param view Refreshed view
     */
    @Override
    public void onRefreshStarted(View view) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeList.discardUndo();
            }

            @Override
            protected Void doInBackground(Void... params) {
                CardManager.update(MainActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (mAdapter == null)
                    initAdapter();
                else
                    mAdapter.notifyDataSetChanged();
                mPullToRefreshLayout.setRefreshComplete();
                if (!registered && !NetUtils.isConnected(MainActivity.this)) {
                    registerReceiver(connectivityChangeReceiver,
                            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                    registered = true;
                }
            }
        }.execute();
    }

    BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isConnected(context)) {
                refreshCards();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        unregisterReceiver(connectivityChangeReceiver);
                        registered = false;
                    }
                });
            }
        }
    };
}
