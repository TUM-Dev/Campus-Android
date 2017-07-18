package de.tum.in.tumcampusapp.activities;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.databinding.ActivityCardsDetailBinding;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.cards.StudyCard;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;

public class CardsDetailActivity extends AppCompatActivity {
    StudyCard card;
    ActivityCardsDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.card = new StudyCard();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cards_detail);
        binding.setCard(card);

        // TODO handle different states (add/view/edit)
        setTitle("add Card");

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
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
                if (save()) {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean save() {
        if (card.is_valid()) {
            try {
                final ChatVerification v = new ChatVerification(this, Utils.getSetting(this, Const.CHAT_MEMBER, ChatMember.class));
                final Context c = this;

                AsyncTask t = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            TUMCabeClient.getInstance(c).addStudyCard(card, v);
                        } catch (IOException e) {
                            System.out.println(e.toString());
                            e.printStackTrace();
                        }
                        return "Uploaded";
                    }
                };
                t.execute("");
            } catch (NoPrivateKey noPrivateKey) {
                noPrivateKey.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }
}
