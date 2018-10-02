package de.tum.in.tumcampusapp.component.ui.studycard;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.ui.studycard.model.StudyCard;
import de.tum.in.tumcampusapp.utils.Utils;

public class CardsQuizActivity extends ActivityForLoadingInBackground<Void, List<StudyCard>> {

    List<StudyCard> cards;
    int nextCard;
    //public final ObservableField<Boolean> isShowingAnswer = new ObservableField<>(false);

    public CardsQuizActivity() {
        super(R.layout.activity_cards_quiz);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startLoading();
    }

    public void setUpLayout() {
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_cards_quiz);
        //binding.setHandler(this);
    }

    @Override
    public void setUpToolbar() {
        super.setUpToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        }
    }

    @Override
    protected List<StudyCard> onLoadInBackground(Void... arg) {
        List<StudyCard> cards = new ArrayList<StudyCard>();
        try {
            cards = TUMCabeClient.getInstance(this)
                                 .getStudyCards();
        } catch (IOException e) {
            Utils.log(e);
        }
        return cards;
    }

    @Override
    protected void onLoadFinished(List<StudyCard> cards) {
        showLoadingEnded();
        if (cards.size() == 0) {
            finish();
            Toast toast = Toast.makeText(this, "No Cards in Quiz", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            this.cards = cards;
            this.nextCard = 0;
            setUpNextCard();
        }
    }

    public void setUpNextCard() {
        if (this.nextCard >= this.cards.size()) {
            finish();
            Toast toast = Toast.makeText(this, "Finished Quiz", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            //this.setTitle((this.nextCard + 1) + "/" + this.cards.size() + ": " + this.cards.get(this.nextCard)
            //                                                                               .getTitle());
            //this.isShowingAnswer.set(false);
            //binding.setCardView(this.cards.get(this.nextCard));
            this.nextCard++;
        }
    }

    /*public void showAnswer() {
        this.isShowingAnswer.set(true);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
