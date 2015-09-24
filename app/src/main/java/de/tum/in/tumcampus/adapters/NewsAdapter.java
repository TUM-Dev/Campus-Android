package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.NewsCard;

public class NewsAdapter extends RecyclerView.Adapter<NewsCard.CardViewHolder> {
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
            card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_news_film_item, parent, false);
        } else {
            card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_news_item, parent, false);
        }
        NewsViewHolder holder = new NewsViewHolder(card);
        holder.title = (TextView) card.findViewById(R.id.news_title);
        holder.img = (ImageView) card.findViewById(R.id.news_img);
        holder.src_date = (TextView) card.findViewById(R.id.news_src_date);
        holder.src_icon = (ImageView) card.findViewById(R.id.news_src_icon);
        holder.src_title = (TextView) card.findViewById(R.id.news_src_title);
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
            title = title.replaceAll("^[0-9]+\\. [0-9]+\\. [0-9]+:[ ]*","");
        }
        holder.title.setText(title);

        // Adds date
        String date = cursor.getString(5);
        Date d = Utils.getISODateTime(date);
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        holder.src_date.setText(sdf.format(d));

        holder.src_title.setText(cursor.getString(8));
        String icon = cursor.getString(7);
        if (icon.isEmpty() || icon.equals("null")) {
            holder.src_icon.setImageResource(R.drawable.ic_comment);
        } else {
            net.loadAndSetImage(icon, holder.src_icon);
        }
    }

    @Override
    public NewsCard.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return NewsCard.inflateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(NewsCard.CardViewHolder holder, int position) {
        NewsViewHolder nHolder = (NewsViewHolder) holder;
        NewsCard card = new NewsCard(mContext);
        card.setNews(c, position);
        nHolder.setCurrentCard(card);

        c.moveToPosition(position);
        bindNewsView(net, holder, c);
    }

    @Override
    public int getItemViewType(int position) {
        c.moveToPosition(position);
        return c.getString(1).equals("2") ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return c.getCount();
    }

    public static class NewsViewHolder extends Card.CardViewHolder {
        ImageView img;
        TextView title;
        TextView src_date;
        TextView src_title;
        ImageView src_icon;

        public NewsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
