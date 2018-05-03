package de.tum.in.tumcampusapp.component.ui.news;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Booleans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends ActivityForDownloadingExternal implements DialogInterface.OnMultiChoiceClickListener {

    private RecyclerView recyclerView;
    private int state = -1;
    private NewsController newsController;

    public NewsActivity() {
        super(Const.NEWS, R.layout.activity_news);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestDownload(false);
        showLoadingEnded();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Gets all news from database
        newsController = new NewsController(this);
        List<News> news = newsController.getAllFromDb(this);

        if (!news.isEmpty()) {
            NewsAdapter adapter = new NewsAdapter(this, news);

            recyclerView = findViewById(R.id.activity_news_list_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            int spacing = Math.round(getResources().getDimension(R.dimen.material_card_view_padding));
            recyclerView.addItemDecoration(new EqualSpacingItemDecoration(spacing));

            /* Restore previous state (including selected item index and scroll position) */
            if (state == -1) {
                recyclerView.scrollToPosition(newsController.getTodayIndex());
            } else {
                recyclerView.scrollToPosition(state);
            }
        } else if (NetUtils.isConnected(this)) {
            showErrorLayout();
        } else {
            showNoInternetLayout();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        List<NewsSources> newsSources = newsController.getNewsSources();

        if (which < newsSources.size()) {
            String key = "news_source_" + newsSources.get(which).getId();
            Utils.setSetting(this, key, isChecked);

            if (recyclerView != null) { //We really don't care if the recyclerView is null, if the position can't be saved. Rather not have the app crash here
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                state = layoutManager.findFirstVisibleItemPosition();
            }

            requestDownload(false);
        }
    }

    /**
     * Save ListView state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        state = layoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_disable_sources) {
            Collection<CharSequence> itemsList = new ArrayList<>();
            Collection<Boolean> checkedList = new ArrayList<>();
            List<NewsSources> newsSources = newsController.getNewsSources();
            // Populate the settingsPrefix dialog from the NewsController sources
            for (NewsSources newsSource : newsSources) {
                itemsList.add(newsSource.getTitle());
                checkedList.add(Utils.getSettingBool(this, "news_source_" + newsSource.getId(), true));
            }

            CharSequence[] items = Iterables.toArray(itemsList, CharSequence.class);
            boolean[] checkedItems = Booleans.toArray(checkedList);

            new AlertDialog.Builder(this)
                    .setMultiChoiceItems(items, checkedItems, this)
                    .create()
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

}