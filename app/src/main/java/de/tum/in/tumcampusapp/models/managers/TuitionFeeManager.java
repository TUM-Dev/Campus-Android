package de.tum.in.tumcampusapp.models.managers;

import android.content.Context;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.Card;
import de.tum.in.tumcampusapp.cards.TuitionFeesCard;
import de.tum.in.tumcampusapp.models.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * Tuition manager, handles tuition card
 */
public class TuitionFeeManager implements Card.ProvidesCard {

    /**
     * Shows tuition card with current fee status
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        try {
            TUMOnlineRequest<TuitionList> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.TUITION_FEE_STATUS, context, true);
            TuitionList tuitionList = requestHandler.fetch();
            if(tuitionList==null)
                return;
            TuitionFeesCard card = new TuitionFeesCard(context);
            card.setTuition(tuitionList.getTuitions().get(0));
            card.apply();
        } catch (Exception e) {
            Utils.log(e);
        }
    }
}
