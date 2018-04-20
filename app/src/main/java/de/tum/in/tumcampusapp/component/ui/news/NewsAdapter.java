package de.tum.in.tumcampusapp.component.ui.news;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.tufilm.FilmCard;
import de.tum.in.tumcampusapp.database.TcaDb;

public class NewsAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");
    private final List<News> news;
    private final Context mContext;

    NewsAdapter(Context context, List<News> news) {
        this.mContext = context;
        this.news = news;
    }

    public static NewsViewHolder newNewsView(ViewGroup parent, boolean isFilm) {
        View card;
        if (isFilm) {
            card = LayoutInflater.from(parent.getContext())
                                 .inflate(R.layout.card_news_film_item, parent, false);
        } else {
            card = LayoutInflater.from(parent.getContext())
                                 .inflate(R.layout.card_news_item, parent, false);
        }
        NewsViewHolder holder = new NewsViewHolder(card);
        holder.title = card.findViewById(R.id.news_title);
        holder.img = card.findViewById(R.id.news_img);
        holder.srcDate = card.findViewById(R.id.news_src_date);
        holder.srcIcon = card.findViewById(R.id.news_src_icon);
        holder.srcTitle = card.findViewById(R.id.news_src_title);
        card.setTag(holder);
        return holder;
    }

    public static void bindNewsView(RecyclerView.ViewHolder newsViewHolder, News news, Context context) {
        NewsViewHolder holder = (NewsViewHolder) newsViewHolder;
        NewsSourcesDao newsSourcesDao = TcaDb.getInstance(context).newsSourcesDao();
        NewsSources newsSource = newsSourcesDao.getNewsSource(Integer.parseInt(news.getSrc()));
        // Set image
        String imgUrl = news.getImage();
        if (imgUrl.isEmpty() || imgUrl.equals("null")) {
            holder.img.setVisibility(View.GONE);
        } else {
            holder.img.setVisibility(View.VISIBLE);
            Picasso p = Picasso.get();
            p.setIndicatorsEnabled(true);
            p.load(imgUrl).into(holder.img);
        }

        String title = news.getTitle();
        if (news.isFilm()) {
            title = COMPILE.matcher(title)
                           .replaceAll("");
        }
        holder.title.setText(title);

        // Adds date
        Date date = news.getDate();
        DateFormat sdf = DateFormat.getDateInstance();
        holder.srcDate.setText(sdf.format(date));

        holder.srcTitle.setText(newsSource.getTitle());
        String icon = newsSource.getIcon();
        if (icon.isEmpty() || "null".equals(icon)) {
            holder.srcIcon.setImageResource(R.drawable.ic_comment);
        } else {
            Picasso.get().load(icon).into(holder.srcIcon);
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return NewsCard.inflateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        NewsViewHolder nHolder = (NewsViewHolder) holder;
        NewsCard card;
        if (news.get(position).isFilm()) {
            card = new FilmCard(mContext);
        } else {
            card = new NewsCard(mContext);
        }
        card.setNews(news.get(position));
        nHolder.setCurrentCard(card);

        bindNewsView(holder, news.get(position), mContext);
    }

    @Override
    public int getItemViewType(int position) {
        return news.get(position).isFilm() ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    private static class NewsViewHolder extends CardViewHolder {
        ImageView img;
        TextView title;
        TextView srcDate;
        TextView srcTitle;
        ImageView srcIcon;

        NewsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
