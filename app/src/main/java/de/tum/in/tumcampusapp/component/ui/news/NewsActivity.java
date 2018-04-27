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
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends ActivityForDownloadingExternal implements DialogInterface.OnMultiChoiceClickListener {

    private RecyclerView lv;
    private int state = -1;
    private NewsController nm;

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
        nm = new NewsController(this);
        List<News> news = nm.getAllFromDb(this);

        if (news.size() > 0) {
            NewsAdapter adapter = new NewsAdapter(this, news);

            lv = findViewById(R.id.activity_news_list_view);
            lv.setLayoutManager(new LinearLayoutManager(this));
            lv.setAdapter(adapter);

            /* Restore previous state (including selected item index and scroll position) */
            if (state == -1) {
                lv.scrollToPosition(nm.getTodayIndex());
            } else {
                lv.scrollToPosition(state);
            }

        } else if (NetUtils.isConnected(this)) {
            showErrorLayout();
        } else {
            showNoInternetLayout();
        }
    }

    /**
     * Save ListView state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LinearLayoutManager layoutManager = (LinearLayoutManager) lv.getLayoutManager();
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
            List<NewsSources> newsSources = nm.getNewsSources();
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

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        List<NewsSources> newsSources = nm.getNewsSources();

        if (which < newsSources.size()) {
            Utils.setSetting(this, "news_source_" + newsSources.get(which)
                                                               .getId(), isChecked);

            if (lv != null) { //We really don't care if the lv is null, if the position can't be saved. Rather not have the app crash here
                LinearLayoutManager layoutManager = (LinearLayoutManager) lv.getLayoutManager();
                state = layoutManager.findFirstVisibleItemPosition();
            }

            requestDownload(false);
        }
    }
}