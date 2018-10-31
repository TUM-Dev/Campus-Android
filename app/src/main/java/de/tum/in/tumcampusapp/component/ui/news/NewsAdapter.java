package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.model.News;

public class NewsAdapter extends RecyclerView.Adapter<NewsViewHolder> {

    private final List<News> news;
    private final NewsInflater newsInflater;

    NewsAdapter(Context context, List<News> news) {
        this.news = news;
        this.newsInflater = new NewsInflater(context);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return newsInflater.onCreateNewsView(parent, viewType, false);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        newsInflater.onBindNewsView(holder, news.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        News newsItem = news.get(position);

        if (newsItem.isFilm()) {
            return R.layout.card_news_film_item;
        } else {
            return R.layout.card_news_item;
        }
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

}
