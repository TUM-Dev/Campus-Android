package de.tum.in.tumcampusapp.component.ui.overview;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.service.DownloadService;
import de.tum.in.tumcampusapp.service.SilenceService;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Main activity displaying the cards and providing navigation with navigation drawer
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Navigation Drawer
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mIsConnectivityChangeReceiverRegistered;

    /**
     * Card list
     */
    private RecyclerView mCardsView;
    private CardAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MainActivityViewModel mViewModel;

    final BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isConnected(context)) {
                refreshCards();
                runOnUiThread(() -> {
                    unregisterReceiver(connectivityChangeReceiver);
                    mIsConnectivityChangeReceiverRegistered = false;
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
        mSwipeRefreshLayout = findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200);

        // Setup card RecyclerView
        mCardsView = findViewById(R.id.cards_view);
        registerForContextMenu(mCardsView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCardsView.setLayoutManager(layoutManager);
        mCardsView.setHasFixedSize(true);

        mAdapter = new CardAdapter();
        mCardsView.setAdapter(mAdapter);

        // Add equal spacing between CardViews in the RecyclerView
        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        mCardsView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        // Swipe gestures
        new ItemTouchHelper(new MainActivityTouchHelperCallback()).attachToRecyclerView(mCardsView);

        // Start silence Service (if already started it will just invoke a check)
        Intent service = new Intent(this, SilenceService.class);
        this.startService(service);

        downloadNewsAlert();

        // Set the list's click listener
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                MainActivity.this.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                MainActivity.this.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mViewModel = ViewModelProviders
                .of(this)
                .get(MainActivityViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getCards().observe(this, cards -> {
            if (cards != null) {
                onNewCardsAvailable(cards);
            }
        });
    }

    private void onNewCardsAvailable(List<Card> cards) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.updateItems(cards);

        if (!NetUtils.isConnected(this) && !mIsConnectivityChangeReceiverRegistered) {
            registerReceiver(connectivityChangeReceiver,
                             new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            mIsConnectivityChangeReceiverRegistered = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater()
            .inflate(R.menu.menu_main, menu);
        return true;
    }

    public void downloadNewsAlert(){
        Intent downloadService = new Intent();
        downloadService.putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL);
        DownloadService.enqueueWork(getBaseContext(), downloadService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsConnectivityChangeReceiverRegistered) {
            unregisterReceiver(connectivityChangeReceiver);
            mIsConnectivityChangeReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(Utils.getSettingBool(this, Const.REFRESH_CARDS, false)){
            refreshCards();
            Utils.setSetting(this, Const.REFRESH_CARDS, false);
        }
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
     * Handle expansion of navigation drawer
     *
     * @param item Clicked menu item
     * @return True if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Show progress indicator and start updating cards in background
     */
    public void refreshCards() {
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();
        downloadNewsAlert();
    }

    /**
     * Starts updating cards in background
     * Called when {@link SwipeRefreshLayout} gets triggered.
     */
    @Override
    public void onRefresh() {
        mViewModel.refreshCards();
    }

    /**
     * Executed when the RestoreCard is pressed
     */
    public void restoreCards(View view) {
        CardManager.restoreCards(this);
        refreshCards();
    }

    /**
     * Smoothly scrolls the RecyclerView to the top and dispatches nestedScrollingEvents to show
     * the Toolbar
     */
    private void showToolbar() {
        mCardsView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        mCardsView.dispatchNestedFling(0, Integer.MIN_VALUE, true);
        mCardsView.stopNestedScroll();
        mCardsView.getLayoutManager()
                  .smoothScrollToPosition(mCardsView, null, 0);
    }

    public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
    }

    /**
     * A touch helper class, Handles swipe to dismiss events
     */
    private class MainActivityTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        public MainActivityTouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
            if (!cardViewHolder.getCurrentCard()
                               .isDismissible()) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
            final Card card = cardViewHolder.getCurrentCard();
            final int lastPos = cardViewHolder.getAdapterPosition();
            mAdapter.remove(lastPos);

            final View coordinatorLayoutView = findViewById(R.id.coordinator);

            Snackbar.make(coordinatorLayoutView, R.string.card_dismissed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> {
                        mAdapter.insert(lastPos, card);
                        mCardsView.getLayoutManager()
                                  .smoothScrollToPosition(mCardsView, null, lastPos);
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                //DISMISS_EVENT_ACTION means, the snackbar was dismissed via the undo button
                                //and therefore, we didn't really dismiss the card
                                card.discardCard();
                            }
                        }
                    })
                    .show();
        }

    }

}
