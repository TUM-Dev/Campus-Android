package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.notifications.model.AppNotification;
import de.tum.in.tumcampusapp.component.notifications.NotificationsProvider;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Response;

/**
 * Tuition manager, handles tuition card
 */
public class TuitionFeeManager implements ProvidesCard, ProvidesNotifications {

    private Context mContext;

    public TuitionFeeManager(Context context) {
        mContext = context;
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();
        Tuition tuition = loadTuition(cacheControl);

        TuitionFeesCard card = new TuitionFeesCard(mContext);
        card.setTuition(tuition);

        results.add(card.getIfShowOnStart());
        return results;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(mContext, "card_tuition_fee_phone", true);
    }

    @NotNull
    @Override
    public List<AppNotification> getNotifications() {
        Tuition tuition = loadTuition(CacheControl.USE_CACHE);
        if (tuition == null) {
            return new ArrayList<>();
        }

        NotificationsProvider provider = new TuitionFeesNotificationsProvider(mContext, tuition);
        return provider.getNotifications();
    }

    @Nullable
    public Tuition loadTuition(CacheControl cacheControl) {
        try {
            Response<TuitionList> response = TUMOnlineClient
                    .getInstance(mContext)
                    .getTuitionFeesStatus(cacheControl)
                    .execute();

            if (response == null || !response.isSuccessful()) {
                return null;
            }

            TuitionList tuitionList = response.body();
            if (tuitionList == null || tuitionList.getTuitions().isEmpty()) {
                return null;
            }

            return tuitionList.getTuitions().get(0);
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

}
