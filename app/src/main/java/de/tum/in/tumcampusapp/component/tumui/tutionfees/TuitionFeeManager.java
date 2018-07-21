package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Response;

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
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();

        try {
            Response<TuitionList> response = TUMOnlineClient
                    .getInstance(mContext)
                    .getTuitionFeesStatus(cacheControl)
                    .execute();

            if (response == null || !response.isSuccessful()) {
                return results;
            }

            TuitionList tuitionList = response.body();
            if (tuitionList == null) {
                return results;
            }

            Tuition tuition = tuitionList.getTuitions().get(0);
            TuitionFeesCard card = new TuitionFeesCard(mContext);
            card.setTuition(tuition);
            results.add(card.getIfShowOnStart());
        } catch (IOException e) {
            Utils.log(e);
        }

        return results;
    }

}
