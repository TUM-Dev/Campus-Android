package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.cards.Card;
import de.tum.in.tumcampus.cards.TuitionFeesCard;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;

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
            TUMOnlineRequest requestHandler = new TUMOnlineRequest(TUMOnlineConst.STUDIENBEITRAGSTATUS, context, true);
            String rawResp = requestHandler.fetch();
            Serializer serializer = new Persister();
            TuitionList tuitionList;
            tuitionList = serializer.read(TuitionList.class, rawResp);
            TuitionFeesCard card = new TuitionFeesCard(context);
            card.setTuition(tuitionList.getTuitions().get(0));
            card.apply();
        } catch (Exception e) {
            Utils.log(e);
        }
    }
}
