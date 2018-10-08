package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Context;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.notifications.NotificationScheduler;
import de.tum.in.tumcampusapp.component.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.notifications.persistence.NotificationType;
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

        if (tuition == null) {
            return results;
        }


        TuitionFeesCard card = new TuitionFeesCard(mContext);
        card.setTuition(tuition);

        results.add(card.getIfShowOnStart());
        return results;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(mContext, "card_tuition_fee_phone", true);
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

            Tuition tuition = tuitionList.getTuitions().get(0);
            if (!tuition.isPaid() && hasNotificationsEnabled()) {
                scheduleNotificationAlarm(tuition);
            }

            return tuitionList.getTuitions().get(0);
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    private void scheduleNotificationAlarm(Tuition tuition) {
        DateTime notificationTime =
                TuitionNotificationScheduler.INSTANCE.getNextNotificationTime(tuition);

        NotificationScheduler scheduler = new NotificationScheduler(mContext);
        scheduler.scheduleAlarm(NotificationType.TUITION_FEES, notificationTime);
    }

}
