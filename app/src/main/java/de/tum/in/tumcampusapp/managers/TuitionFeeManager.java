package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.cards.TuitionFeesCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.models.tumo.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * Tuition manager, handles tuition card
 */
public class TuitionFeeManager implements Card.ProvidesCard {

    /**
     * Shows tuition card with current fee status
     *
     * @param context Context
     */
    @Override
    public void onRequestCard(Context context) {
        TUMOnlineRequest<TuitionList> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getTUITION_FEE_STATUS(), context, true);
        Optional<TuitionList> tuitionList = requestHandler.fetch();
        if (!tuitionList.isPresent()) {
            return;
        }
        TuitionFeesCard card = new TuitionFeesCard(context);
        card.setTuition(tuitionList.get()
                                   .getTuitions()
                                   .get(0));
        card.apply();
    }
}
