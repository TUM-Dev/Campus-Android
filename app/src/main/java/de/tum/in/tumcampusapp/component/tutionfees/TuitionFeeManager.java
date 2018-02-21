package de.tum.in.tumcampusapp.component.tutionfees;

import android.content.Context;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.component.generic.card.generic.Card;
import de.tum.in.tumcampusapp.component.tutionfees.model.TuitionList;

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
