package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

        TuitionList tuitionList = TUMOnlineClient
                .getInstance(mContext)
                .getTuitionFeesStatus()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnError(Utils::log)
                .blockingGet();

        if (tuitionList != null) {
            TuitionFeesCard card = new TuitionFeesCard(mContext);
            card.setTuition(tuitionList.getTuitions().get(0));
            results.add(card.getIfShowOnStart());
        }

        return results;
    }

}
