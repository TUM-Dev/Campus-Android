package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.ui.overview.CardManager;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

/**
 * Card that shows information about your fees that have to be paid or have been paid
 */
public class TuitionFeesCard extends Card {

    private static final String LAST_FEE_FRIST = "fee_frist";
    private static final String LAST_FEE_SOLL = "fee_soll";

    private Tuition mTuition;

    public TuitionFeesCard(Context context) {
        super(CardManager.CARD_TUITION_FEE, context, "card_tuition_fee");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_tuition_fees, parent, false);
        return new CardViewHolder(view);
    }

    public String getTitle() {
        return getContext().getString(R.string.tuition_fees);
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Intent getIntent() {
        return new Intent(getContext(), TuitionFeesActivity.class);
    }

    @Override
    public void updateViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        TextView reregisterInfoTextView =
                viewHolder.itemView.findViewById(R.id.reregister_info_text_view);
        TextView outstandingBalanceTextView =
                viewHolder.itemView.findViewById(R.id.outstanding_balance_text_view);

        if (mTuition.isPaid()) {
            String placeholderText = getContext().getString(R.string.reregister_success);
            String text = String.format(placeholderText, mTuition.getSemester());
            reregisterInfoTextView.setText(text);
        } else {
            DateTime date = mTuition.getDeadline();
            String dateText = DateTimeFormat.mediumDate().print(date);

            String text = String.format(getContext().getString(R.string.reregister_todo), dateText);
            reregisterInfoTextView.setText(text);

            String textWithPlaceholder = getContext().getString(R.string.amount_dots_card);
            String balanceText = String.format(textWithPlaceholder, mTuition.getAmountText(getContext()));
            outstandingBalanceTextView.setText(balanceText);
            outstandingBalanceTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        String prevDeadline = prefs.getString(LAST_FEE_FRIST, "");
        String prevAmount = prefs.getString(LAST_FEE_SOLL, Float.toString(mTuition.getAmount()));

        // If app gets started for the first time and fee is already paid don't annoy user
        // by showing him that he has been re-registered successfully
        String deadline = DateTimeUtils.INSTANCE.getDateString(mTuition.getDeadline());
        String amount = Float.toString(mTuition.getAmount());
        return !(prevDeadline.isEmpty() && mTuition.isPaid()) &&
               (prevDeadline.compareTo(deadline) < 0 || prevAmount.compareTo(amount) > 0);
    }

    public void discard(Editor editor) {
        String deadline = DateTimeUtils.INSTANCE.getDateString(mTuition.getDeadline());
        String amount = Float.toString(mTuition.getAmount());
        editor.putString(LAST_FEE_FRIST, deadline);
        editor.putString(LAST_FEE_SOLL, amount);
    }

    public void setTuition(Tuition tuition) {
        mTuition = tuition;
    }

}
