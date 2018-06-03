package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;

import com.google.common.base.Optional;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;

/**
 * Tuition manager, handles tuition card
 */
public class TuitionFeeManager implements ProvidesCard {

    private Context mContext;

    public TuitionFeeManager(Context context) {
        mContext = context;
    }

    @NotNull
    @Override
    public List<Card> getCards() {
        List<Card> results = new ArrayList<>();

        TUMOnlineRequest<TuitionList> requestHandler =
                new TUMOnlineRequest<>(TUMOnlineConst.TUITION_FEE_STATUS, mContext, true);

        Optional<TuitionList> tuitionList = requestHandler.fetch();
        if (!tuitionList.isPresent()) {
            return results;
        }

        TuitionFeesCard card = new TuitionFeesCard(mContext);
        card.setTuition(tuitionList.get()
                                   .getTuitions()
                                   .get(0));

        results.add(card.getIfShowOnStart());
        return results;
    }

}
