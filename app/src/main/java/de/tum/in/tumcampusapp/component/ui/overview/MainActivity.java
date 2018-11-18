package de.tum.in.tumcampusapp.component.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
public class MainActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, CardInteractionListener {

    private boolean mIsConnectivityChangeReceiverRegistered;

    private RecyclerView mCardsView;
    private CardAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MainActivityViewModel mViewModel;

    ConnectivityManager connectivityManager;
    final NetworkCallback networkCallback = new NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            runOnUiThread(MainActivity.this::refreshCards);
        }
    };

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Setup pull to refresh
        mSwipeRefreshLayout = findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200);

        // Setup card RecyclerView
        mCardsView = findViewById(R.id.cards_view);
        registerForContextMenu(mCardsView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mCardsView.setLayoutManager(layoutManager);
        mCardsView.setHasFixedSize(true);

        mAdapter = new CardAdapter();
        mCardsView.setAdapter(mAdapter);

        showToolbar();

        // Add equal spacing between CardViews in the RecyclerView
        int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
        mCardsView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

        // Swipe gestures
        new ItemTouchHelper(new MainActivityTouchHelperCallback()).attachToRecyclerView(mCardsView);

        // Start silence Service (if already started it will just invoke a check)
        Intent service = new Intent(this, SilenceService.class);
        this.startService(service);

        mViewModel = ViewModelProviders
                .of(this)
                .get(MainActivityViewModel.class);

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
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetUtils.getInternetCapability())
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
            mIsConnectivityChangeReceiverRegistered = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void downloadNewsAlert() {
        Intent downloadService = new Intent();
        downloadService.putExtra(Const.ACTION_EXTRA, Const.DOWNLOAD_ALL_FROM_EXTERNAL);
        DownloadService.enqueueWork(this, downloadService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsConnectivityChangeReceiverRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            mIsConnectivityChangeReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utils.getSettingBool(this, Const.REFRESH_CARDS, false)) {
            refreshCards();
            Utils.setSetting(this, Const.REFRESH_CARDS, false);
        }
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

        RecyclerView.LayoutManager layoutManager = mCardsView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.smoothScrollToPosition(mCardsView, null, 0);
        }
    }

    @Override
    public void onAlwaysHideCard(int position) {
        mAdapter.remove(position);
    }

    /**
     * A touch helper class, Handles swipe to dismiss events
     */
    private class MainActivityTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        MainActivityTouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
            CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
            Card card = cardViewHolder.getCurrentCard();
            if (card == null || !card.isDismissible()) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
            final Card card = cardViewHolder.getCurrentCard();
            final int lastPos = cardViewHolder.getAdapterPosition();
            mAdapter.remove(lastPos);

            final View coordinatorLayoutView = findViewById(R.id.coordinator);
            final int color = ContextCompat.getColor(getBaseContext(), android.R.color.white);

            Snackbar.make(coordinatorLayoutView, R.string.card_dismissed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> {
                        mAdapter.insert(lastPos, card);

                        RecyclerView.LayoutManager layoutManager = mCardsView.getLayoutManager();
                        if (layoutManager != null) {
                            layoutManager.smoothScrollToPosition(mCardsView, null, lastPos);
                        }
                    })
                    .setActionTextColor(color)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                // DISMISS_EVENT_ACTION means, the snackbar was dismissed via the undo button
                                // and therefore, we didn't really dismiss the card

                                if (card != null) {
                                    card.discardCard();
                                }
                            }
                        }
                    })
                    .show();
        }
    }
}
