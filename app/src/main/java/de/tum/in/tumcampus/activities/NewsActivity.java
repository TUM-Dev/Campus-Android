package de.tum.in.tumcampus.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.NewsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.NewsManager;

/**
 * Activity to show News (message, image, date)
 */
public class NewsActivity extends ActivityForDownloadingExternal {

    private RecyclerView lv;
    private int state = -1;
    private NewsManager nm;

    public NewsActivity() {
        super(Const.NEWS, R.layout.activity_news);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestDownload(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Gets all news from database
        nm = new NewsManager(this);
        Cursor cursor = nm.getAllFromDb(this);
        if (cursor.getCount() > 0) {
            NewsAdapter adapter = new NewsAdapter(this, cursor);

            lv = (RecyclerView) findViewById(R.id.activity_news_list_view);
            lv.setLayoutManager(new LinearLayoutManager(this));
            lv.setAdapter(adapter);

            /** Restore previous state (including selected item index and scroll position) */
            if (state != -1) {
                lv.scrollToPosition(state);
            } else {
                lv.scrollToPosition(nm.getTodayIndex());
            }


        } else if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
        } else {
            showErrorLayout();
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
        Cursor cur = nm.getNewsSources();
        int i = 0;
        if (cur.moveToFirst()) {
            do {
                MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, cur.getString(2));
                item.setCheckable(true);
                boolean checked = Utils.getSettingBool(this, "news_source_" + cur.getString(0), true);
                item.setChecked(checked);
                i++;
            } while (cur.moveToNext());
        }
        cur.close();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Cursor cur = nm.getNewsSources();
        if (item.getItemId() < cur.getCount()) {
            if (cur.moveToPosition(item.getItemId())) {
                boolean checked = !item.isChecked();
                Utils.setSetting(this, "news_source_" + cur.getString(0), checked);
                item.setChecked(checked);
                LinearLayoutManager layoutManager = (LinearLayoutManager) lv.getLayoutManager();
                state = layoutManager.findFirstVisibleItemPosition();
                requestDownload(false);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}