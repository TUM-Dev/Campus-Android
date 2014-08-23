package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.cards.TuitionFeesCard;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequest;


public class TuitionFeeManager implements ProvidesCard {
    @Override
    public void onRequestCard(Context context) {
        try {
            TUMOnlineRequest requestHandler = new TUMOnlineRequest(Const.STUDIENBEITRAGSTATUS, context);
            String rawResp = requestHandler.fetch();
            Serializer serializer = new Persister();
            TuitionList tuitionList;
            tuitionList = serializer.read(TuitionList.class, rawResp);
            TuitionFeesCard card = new TuitionFeesCard(context);
            card.setTuition(tuitionList.getTuitions().get(0));
            card.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
