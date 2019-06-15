package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Nullable;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.navigation.NavDestination;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsAlert;
import de.tum.in.tumcampusapp.component.ui.overview.CardInteractionListener;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;

/**
 * Shows important news
 */
public class TopNewsCard extends Card {
    private ImageView imageView;
    private ProgressBar progress;
    private TopNewsStore topNewsStore;
    private NewsAlert newsAlert;

    public TopNewsCard(Context context) {
        super(CardManager.CARD_TOP_NEWS, context, "top_news");
        this.topNewsStore = new RealTopNewsStore(PreferenceManager.getDefaultSharedPreferences(context));
        this.newsAlert = topNewsStore.getNewsAlert();
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent,
                                                   CardInteractionListener interactionListener) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_top_news, parent, false);
        return new CardViewHolder(view, interactionListener);
    }

    private void updateImageView() {
        if (newsAlert == null || newsAlert.getUrl().isEmpty() || imageView == null) {
            return;
        }

        Picasso.get()
                .load(newsAlert.getUrl())
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // remove progress bar
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        discard();
                    }
                });
    }

    @Override
    public int getId() {
        return 0;
    }

    @Nullable
    @Override
    public NavDestination getNavigationDestination() {
        if (newsAlert == null || newsAlert.getLink().isEmpty()) {
            return null;
        }

        return new NavDestination.Link(newsAlert.getLink());
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        imageView = viewHolder.itemView.findViewById(R.id.top_news_img);
        progress = viewHolder.itemView.findViewById(R.id.top_news_progress);
        updateImageView();
    }

    @Override
    protected boolean shouldShow(@NonNull SharedPreferences prefs) {
        if (newsAlert == null) {
            return false;
        }

        return topNewsStore.isEnabled() && newsAlert.getShouldDisplay();
    }

    @Override
    public void discard(@NonNull SharedPreferences.Editor editor) {
        topNewsStore.setEnabled(false);
    }

}
