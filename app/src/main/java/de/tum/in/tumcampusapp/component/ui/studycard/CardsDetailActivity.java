package de.tum.in.tumcampusapp.component.ui.studycard;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoPrivateKey;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeVerification;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.studycard.model.StudyCard;
import de.tum.in.tumcampusapp.databinding.ActivityCardsDetailBinding;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

public class CardsDetailActivity extends ActivityForLoadingInBackground<Void, StudyCard> {
    StudyCard card;

    public CardsDetailActivity() {
        super(R.layout.activity_cards_detail);
        this.card = new StudyCard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO handle different states (add/view/edit)
        setTitle(this.getResources()
                     .getString(R.string.title_activity_cards_detail));
    }

    @Override
    public void setUpLayout() {
        ActivityCardsDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_cards_detail);
        binding.setCard(card);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_cards_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.save_card:
                startLoading();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected StudyCard onLoadInBackground(Void... arg) {
        if (!card.is_valid()) {
            return null;
        }
        try {
            ChatMember chatMember = Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class);
            final TUMCabeVerification v = TUMCabeVerification.create(this, chatMember);
            final Context c = this;
            return TUMCabeClient.getInstance(c)
                                .addStudyCard(card, v);
        } catch (IOException | NoPrivateKey e) {
            Utils.log(e);
        }
        return null;
    }

    @Override
    protected void onLoadFinished(StudyCard card) {
        if (card != null) {
            finish();
        }
    }
}
