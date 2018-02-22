package de.tum.in.tumcampusapp.component.ui.curricula;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.ui.curricula.model.Curriculum;
import de.tum.in.tumcampusapp.utils.NetUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class CurriculaActivity extends ActivityForLoadingInBackground<Void, List<Curriculum>> implements OnItemClickListener {
    public static final String NAME = "name";
    public static final String URL = "url";

    public static final String CURRICULA_URL = "https://tumcabe.in.tum.de/Api/curricula";

    private final Map<String, String> options = new HashMap<>();
    private ArrayList<Curriculum> curriculumList;
    private StickyListHeadersListView list;

    public CurriculaActivity() {
        super(R.layout.activity_curricula);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        curriculumList = new ArrayList<>();

        list = this.findViewById(R.id.activity_curricula_list_view);
        list.setOnItemClickListener(this);

        // Fetch all curricula from webservice via parent async class
        this.startLoading();
    }

    @Override
    protected List<Curriculum> onLoadInBackground(Void... arg) {
        try {
            return TUMCabeClient.getInstance(this)
                                .getAllCurriculas();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    protected void onLoadFinished(List<Curriculum> curricula) {
        if (curricula.isEmpty()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }

        options.clear();
        for (Curriculum curriculum : curricula) {
            curriculumList.add(curriculum);
            options.put(curriculum.getName(), curriculum.getUrl());
        }

        Collections.sort(curriculumList);
        list.setAdapter(new CurriculumAdapter(this, curriculumList));

        showLoadingEnded();
    }

    /**
     * Handle click on curricula item
     *
     * @param parent Containing listView
     * @param view   Item view
     * @param pos    Index of item
     * @param id     Id of item
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        String curriculumName = ((TextView) view).getText()
                                                 .toString();

        // Puts URL and name into an intent and starts the detail view
        Intent intent = new Intent(this, CurriculaDetailsActivity.class);
        intent.putExtra(URL, options.get(curriculumName));
        intent.putExtra(NAME, curriculumName);
        this.startActivity(intent);
    }
}
