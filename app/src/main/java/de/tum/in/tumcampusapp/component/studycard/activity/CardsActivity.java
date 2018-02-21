package de.tum.in.tumcampusapp.component.studycard.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.generic.adapter.NoResultsAdapter;
import de.tum.in.tumcampusapp.component.studycard.adapter.StudyCardListAdapter;
import de.tum.in.tumcampusapp.component.studycard.model.CardActivityState;
import de.tum.in.tumcampusapp.component.studycard.model.StudyCard;
import de.tum.in.tumcampusapp.databinding.ActivityCardsBinding;
import de.tum.in.tumcampusapp.utils.Utils;

public class CardsActivity extends ActivityForLoadingInBackground<Void, List<StudyCard>> implements AdapterView.OnItemClickListener {

    ActivityCardsBinding binding;
    CardActivityState state;

    public CardsActivity() {
        super(R.layout.activity_cards);
        this.state = new CardActivityState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUpLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cards);
        binding.setState(this.state);
        binding.cardList.setOnItemClickListener(this);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(binding.fab.getContext(), CardsQuizActivity.class);
                startActivity(intent);
            }
        });
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
            binding.cardList.setAdapter(new NoResultsAdapter(this));
        } else {
            StudyCardListAdapter adapter = new StudyCardListAdapter(this, cards);
            binding.cardList.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO open edit/view
    }
}