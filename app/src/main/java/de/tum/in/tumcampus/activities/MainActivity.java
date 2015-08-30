package de.tum.in.tumcampus.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.BaseActivity;
import de.tum.in.tumcampus.adapters.CardsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.SwipeDismissList;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.services.SilenceService;

/**
 * Main activity displaying the cards and providing navigation with navigation drawer
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
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
    private RecyclerView mCardsView;
    private CardsAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshlayout;
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

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup pull to refresh
        mSwipeRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
        mSwipeRefreshlayout.setOnRefreshListener(this);
        //TODO: set colors
        //mSwipeRefreshlayout.setColorSchemeResources(R.color.);

        // Setup card RecyclerView
        mCardsView = (RecyclerView) findViewById(R.id.cards_view);
        registerForContextMenu(mCardsView); //TODO

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCardsView.setLayoutManager(layoutManager);
        mCardsView.setHasFixedSize(true);

        ItemTouchHelper touchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        Card.CardViewHolder cardViewHolder = (Card.CardViewHolder) viewHolder;
                        if (!cardViewHolder.getCurrentCard().isDismissable()) {
                            return 0;
                        }
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        //Moving is not allowed as per flags
                        return false;
                    }

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        Card.CardViewHolder cardViewHolder = (Card.CardViewHolder) viewHolder;
                        Card card = cardViewHolder.getCurrentCard();
                        card.discardCard();
                        mAdapter.remove(card);
                    }
                });

        touchHelper.attachToRecyclerView(mCardsView);

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
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
        mAdapter = new CardsAdapter();
        mCardsView.setAdapter(mAdapter);
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

    public void onFabClicked(View v) {
        startActivity(new Intent(this, UserPreferencesActivity.class));
    }

    /**
     * Handle swipe to dismiss events
     *
     * @param listView ListView
     * @param position Swiped item position
     */
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
        Card card = mAdapter.getItem(info.position);
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
        Card card = mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case MENU_OPEN_SETTINGS:
                // Open card's preference screen
                String key = card.getSettings();
                if (key == null)
                    return true;
                Intent intent = new Intent(this, UserPreferencesActivity.class);
                intent.putExtra(Const.PREFERENCE_SCREEN, key);
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
        mSwipeRefreshlayout.setRefreshing(true);
        onRefresh();
    }

    /**
     * Starts updating cards in background
     * Called when {@link SwipeRefreshLayout} gets triggered.
     */
    @Override
    public void onRefresh() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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

                mSwipeRefreshlayout.setRefreshing(false);
                if (!registered && !NetUtils.isConnected(MainActivity.this)) {
                    registerReceiver(connectivityChangeReceiver,
                            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                    registered = true;
                }
            }
        }.execute();
    }

    public void onClick(View view) {
        CardManager.restoreCards();
        refreshCards();
    }
}
