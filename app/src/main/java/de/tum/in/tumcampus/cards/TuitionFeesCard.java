package de.tum.in.tumcampus.cards;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.TuitionFeesActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Tuition;
import de.tum.in.tumcampus.models.managers.CardManager;

/**
 * Card that shows information about your fees that have to be paid or have been paid
 */
public class TuitionFeesCard extends Card {

    private static final String LAST_FEE_FRIST = "fee_frist";
    private static final String LAST_FEE_SOLL = "fee_soll";
    private Tuition mTuition;

    public TuitionFeesCard(Context context) {
        super(context, "card_tuition_fee");
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public int getTyp() {
        return CardManager.CARD_TUITION_FEE;
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.tuition_fees);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        CardViewHolder cardsViewHolder = (CardViewHolder) viewHolder;
        List<View> addedViews = cardsViewHolder.getAddedViews();

        mCard = viewHolder.itemView;
        mLinearLayout = (LinearLayout) mCard.findViewById(R.id.card_view);
        mTitleView = (TextView) mCard.findViewById(R.id.card_title);
        mTitleView.setText(getTitle());

        //Remove additional views
        for (View view : addedViews) {
            mLinearLayout.removeView(view);
        }

        if (mTuition.getSoll().equals("0")) {
            addedViews.add(addTextView(String.format(mContext.getString(R.string.reregister_success), mTuition.getSemesterBez())));
        } else {
            Date d = Utils.getDate(mTuition.getFrist());
            String date = SimpleDateFormat.getDateInstance().format(d);
            addedViews.add(addTextView(String.format(mContext.getString(R.string.reregister_todo), date)));
            addedViews.add(addTextView(viewHolder.itemView.getContext().getString(R.string.amount_dots) + " " + mTuition.getSoll() + "€"));
        }
    }

    @Override
    public void discard(Editor editor) {
        editor.putString(LAST_FEE_FRIST, mTuition.getFrist());
        editor.putString(LAST_FEE_SOLL, mTuition.getSoll());
    }

    @Override
    public boolean shouldShow(SharedPreferences prefs) {
        String prevFrist = prefs.getString(LAST_FEE_FRIST, "");
        String prevSoll = prefs.getString(LAST_FEE_SOLL, mTuition.getSoll());

        // If app gets started for the first time and fee is already paid don't annoy user
        // by showing him that he has been re-registered successfully
        return !(prevFrist.isEmpty() && mTuition.getSoll().equals("0")) &&
                (prevFrist.compareTo(mTuition.getFrist()) < 0 || prevSoll.compareTo(mTuition.getSoll()) > 0);
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        if (mTuition.getSoll().equals("0")) {
            notificationBuilder.setContentText(String.format(mContext.getString(R.string.reregister_success), mTuition.getSemesterBez()));
        } else {
            notificationBuilder.setContentText(mTuition.getSoll() + "€\n" + String.format(mContext.getString(R.string.reregister_todo), mTuition.getFrist()));
        }
        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wear_tuition_fee);
        notificationBuilder.extend(new NotificationCompat.WearableExtender().setBackground(bm));
        return notificationBuilder.build();
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, TuitionFeesActivity.class);
    }

    public void setTuition(Tuition tuition) {
        mTuition = tuition;
    }
}
