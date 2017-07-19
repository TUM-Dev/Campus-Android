package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.adapters.StudyCardListAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.databinding.ActivityCardsBinding;
import de.tum.in.tumcampusapp.models.cards.CardActivityState;
import de.tum.in.tumcampusapp.models.cards.StudyCard;

public class CardsActivity extends ActivityForLoadingInBackground<Void, List<StudyCard>> implements AdapterView.OnItemClickListener {

    private ListView cardList;

    CardActivityState state;
    private StudyCardListAdapter adapter;

    public CardsActivity() {
        super(R.layout.activity_cards);
        this.state = new CardActivityState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bind UI elements
        cardList = (ListView) findViewById(R.id.card_list);
        cardList.setOnItemClickListener(this);
    }

    @Override
    public void setUpLayout() {
        ActivityCardsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_cards);
        binding.setState(this.state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.create_card:
                Intent intent = new Intent(this, CardsDetailActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected List<StudyCard> onLoadInBackground(Void... arg) {
        List<StudyCard> cards = new ArrayList<StudyCard>();
        try {
            cards = TUMCabeClient.getInstance(this).getStudyCards();
        } catch (IOException e) {
            Utils.log(e);
        }
        return cards;
    }

    @Override
    protected void onLoadFinished(List<StudyCard> cards) {
        showLoadingEnded();
        if (cards.size() == 0) {
            cardList.setAdapter(new NoResultsAdapter(this));
        } else {
            // set ListView to data via the LecturesListAdapter
            adapter = new StudyCardListAdapter(this, cards);
            cardList.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO open edit/view
    }
}