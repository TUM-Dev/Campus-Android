package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;

import com.google.common.base.Optional;

<<<<<<< HEAD
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

||||||| merged common ancestors
=======
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

>>>>>>> 6ce4faa2f74ae18d09d52b71074bd044da0ec9bf
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.component.other.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.other.notifications.model.AppNotification;
import de.tum.in.tumcampusapp.component.other.notifications.providers.NotificationsProvider;
import de.tum.in.tumcampusapp.component.other.notifications.providers.TuitionFeesNotificationsProvider;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.utils.Utils;

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
    public List<Card> getCards() {
        List<Card> results = new ArrayList<>();
        Tuition tuition = loadTuition();

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
        Tuition tuition = loadTuition();
        if (tuition == null) {
            return new ArrayList<>();
        }

        NotificationsProvider provider = new TuitionFeesNotificationsProvider(mContext, tuition);
        return provider.getNotifications();
    }

    @Nullable
    public Tuition loadTuition() {
        TUMOnlineRequest<TuitionList> requestHandler =
                new TUMOnlineRequest<>(TUMOnlineConst.TUITION_FEE_STATUS, mContext, true);

        Optional<TuitionList> tuitionList = requestHandler.fetch();
        if (!tuitionList.isPresent()) {
            return null;
        }

        List<Tuition> tuitions = tuitionList.get().getTuitions();
        if (tuitions.isEmpty()) {
            return null;
        } else {
            return tuitions.get(0);
        }
    }

}
