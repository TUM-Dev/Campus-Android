package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.FilmCard;
import de.tum.in.tumcampusapp.cards.NewsCard;
import de.tum.in.tumcampusapp.cards.generic.Card;

public class NewsAdapter extends RecyclerView.Adapter<Card.CardViewHolder> {
    private static final Pattern COMPILE = Pattern.compile("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*");
    private final NetUtils net;
    private final Cursor c;
    private final Context mContext;

    public NewsAdapter(Context context, Cursor c) {
        this.mContext = context;
        net = new NetUtils(context);
        this.c = c;
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

    public static void bindNewsView(NetUtils net, RecyclerView.ViewHolder newsViewHolder, Cursor cursor) {
        NewsViewHolder holder = (NewsViewHolder) newsViewHolder;

        // Set image
        String imgUrl = cursor.getString(4);
        if (imgUrl == null || imgUrl.isEmpty() || imgUrl.equals("null")) {
            holder.img.setVisibility(View.GONE);
        } else {
            holder.img.setVisibility(View.VISIBLE);
            net.loadAndSetImage(imgUrl, holder.img);
        }

        String title = cursor.getString(2);
        if (cursor.getInt(1) == 2) {
            title = COMPILE.matcher(title)
                           .replaceAll("");
        }
        holder.title.setText(title);

        // Adds date
        String date = cursor.getString(5);
        Date d = Utils.getDateTime(date);
        DateFormat sdf = DateFormat.getDateInstance();
        holder.srcDate.setText(sdf.format(d));

        holder.srcTitle.setText(cursor.getString(8));
        String icon = cursor.getString(7);
        if (icon.isEmpty() || "null".equals(icon)) {
            holder.srcIcon.setImageResource(R.drawable.ic_comment);
        } else {
            net.loadAndSetImage(icon, holder.srcIcon);
        }
    }

    @Override
    public Card.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return NewsCard.inflateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(Card.CardViewHolder holder, int position) {
        NewsViewHolder nHolder = (NewsViewHolder) holder;
        NewsCard card;
        if (FilmCard.isNewsAFilm(c, position)) {
            card = new FilmCard(mContext);
        } else {
            card = new NewsCard(mContext);
        }
        card.setNews(c, position);
        nHolder.setCurrentCard(card);

        c.moveToPosition(position);
        bindNewsView(net, holder, c);
    }

    @Override
    public int getItemViewType(int position) {
        c.moveToPosition(position);
        return "2".equals(c.getString(1)) ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return c.getCount();
    }

    private static class NewsViewHolder extends Card.CardViewHolder {
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
