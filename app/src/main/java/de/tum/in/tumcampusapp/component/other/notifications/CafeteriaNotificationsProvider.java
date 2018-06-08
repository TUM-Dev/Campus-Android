package de.tum.in.tumcampusapp.component.other.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.Utils;

public class CafeteriaNotificationsProvider extends NotificationsProvider {

    private CafeteriaWithMenus mCafeteria;

    public CafeteriaNotificationsProvider(@NotNull Context context, CafeteriaWithMenus cafeteria) {
        super(context);
        mCafeteria = cafeteria;
    }

    @NotNull
    @Override
    public List<AppNotification> getNotifications() {
        // TODO: This is the previous implementation, which is way too complicated.
        // Let's refactor this!

        Map<String, String> rolePrices = CafeteriaPrices.INSTANCE.getRolePrices(getContext());

        NotificationCompat.WearableExtender morePageNotification = new NotificationCompat.WearableExtender();

        StringBuilder allContent = new StringBuilder();
        StringBuilder firstContent = new StringBuilder();
        for (CafeteriaMenu menu : mCafeteria.getMenus()) {
            if ("bei".equals(menu.getTypeShort())) {
                continue;
            }

            NotificationCompat.Builder pageNotificationBuilder =
                    new NotificationCompat.Builder(getContext(), Const.NOTIFICATION_CHANNEL_CAFETERIA);

            pageNotificationBuilder.setContentTitle(CafeteriaManager.PATTERN
                    .matcher(menu.getTypeLong())
                    .replaceAll("")
                    .trim()
            );
            pageNotificationBuilder.setSmallIcon(R.drawable.ic_notification);
            pageNotificationBuilder.setLargeIcon(Utils.getLargeIcon(getContext(), R.drawable.ic_cutlery));

            StringBuilder content = new StringBuilder(menu.getName());
            if (rolePrices.containsKey(menu.getTypeLong())) {
                content.append('\n')
                        .append(rolePrices.get(menu.getTypeLong()))
                        .append(" €");
            }

            String contentString = CafeteriaManager.COMPILE
                    .matcher(content.toString())
                    .replaceAll("")
                    .trim();

            pageNotificationBuilder.setContentText(contentString);
            if ("tg".equals(menu.getTypeShort())) {
                if (!allContent.toString()
                        .isEmpty()) {
                    allContent.append('\n');
                }
                allContent.append(contentString);
            }
            if (firstContent.toString().isEmpty()) {
                firstContent.append(
                        CafeteriaManager.COMPILE
                                .matcher(menu.getName())
                                .replaceAll("")
                                .trim()
                ).append('…');
            } else {
                morePageNotification.addPage(pageNotificationBuilder.build());
            }
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.wear_cafeteria);
        morePageNotification.setBackground(bitmap);

        Date date = DateUtils.getDate(mCafeteria.getNextMenuDate());

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getContext(), Const.NOTIFICATION_CHANNEL_CAFETERIA)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(allContent))
                        .setContentText(firstContent)
                        .setWhen(date.getTime());

        Intent intent = mCafeteria.getIntent(getContext());
        if (intent != null) {
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(getContext(), 0, intent, 0);
            notificationBuilder.setContentIntent(pendingIntent);
        }

        Notification notification = notificationBuilder.build();

        List<AppNotification> results = new ArrayList<>();
        results.add(new InstantNotification(notification));
        return results;
    }

}
