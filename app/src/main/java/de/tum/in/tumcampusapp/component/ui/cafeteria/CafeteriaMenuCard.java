package de.tum.in.tumcampusapp.component.ui.cafeteria;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
import de.tum.in.tumcampusapp.component.ui.overview.card.CardViewHolder;
import de.tum.in.tumcampusapp.component.ui.overview.card.NotificationAwareCard;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

import static de.tum.in.tumcampusapp.component.ui.overview.CardManager.CARD_CAFETERIA;

/**
 * Card that shows the cafeteria menu
 */
public class CafeteriaMenuCard extends NotificationAwareCard {
    private static final String CAFETERIA_DATE = "cafeteria_date";
    private static final Pattern COMPILE = Pattern.compile("\\([^\\)]+\\)");
    private static final Pattern PATTERN = Pattern.compile("[0-9]");

    private CafeteriaWithMenus mCafeteria;

    public CafeteriaMenuCard(Context context) {
        super(CARD_CAFETERIA, context, "card_cafeteria");
    }

    public static CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_cafeteria_menu, parent, false);
        return new CafeteriaMenuViewHolder(view);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);

        if (viewHolder instanceof CafeteriaMenuViewHolder) {
            CafeteriaMenuViewHolder holder = (CafeteriaMenuViewHolder) viewHolder;
            holder.bind(mCafeteria);
        }
    }

    /**
     * Sets the information needed to build the card
     *
     * @param cafeteria The CafeteriaWithMenus object to be displayed in the card
     */
    public void setCafeteriaWithMenus(CafeteriaWithMenus cafeteria) {
        this.mCafeteria = cafeteria;
    }

    @Override
    public String getTitle() {
        return mCafeteria.getName();
    }

    @Override
    public Intent getIntent() {
        Intent i = new Intent(getContext(), CafeteriaActivity.class);
        i.putExtra(Const.CAFETERIA_ID, mCafeteria.getId());
        return i;
    }

    @Override
    public void discard(Editor editor) {
        DateTime date = mCafeteria.getNextMenuDate();
        editor.putLong(CAFETERIA_DATE, date.getMillis());
    }

    @Override
    protected boolean shouldShow(SharedPreferences prefs) {
        final long prevDate = prefs.getLong(CAFETERIA_DATE, 0);
        DateTime date = mCafeteria.getNextMenuDate();
        return prevDate < date.getMillis();
    }

    @Override
    protected Notification fillNotification(NotificationCompat.Builder notificationBuilder) {
        Map<String, String> rolePrices = CafeteriaPrices.INSTANCE.getRolePrices(getContext());

        NotificationCompat.WearableExtender morePageNotification = new NotificationCompat.WearableExtender();

        StringBuilder allContent = new StringBuilder();
        StringBuilder firstContent = new StringBuilder();
        for (CafeteriaMenu menu : mCafeteria.getMenus()) {
            if ("bei".equals(menu.getTypeShort())) {
                continue;
            }

            NotificationCompat.Builder pageNotification = new NotificationCompat.Builder(getContext(), Const.NOTIFICATION_CHANNEL_CAFETERIA)
                    .setContentTitle(PATTERN.matcher(menu.getTypeLong())
                    .replaceAll("")
                    .trim());
            pageNotification.setSmallIcon(R.drawable.ic_notification);
            pageNotification.setLargeIcon(Utils.getLargeIcon(getContext(), R.drawable.ic_cutlery));

            StringBuilder content = new StringBuilder(menu.getName());
            if (rolePrices.containsKey(menu.getTypeLong())) {
                content.append('\n')
                       .append(rolePrices.get(menu.getTypeLong()))
                       .append(" €");
            }

            String contentString = COMPILE.matcher(content.toString())
                                          .replaceAll("")
                                          .trim();
            pageNotification.setContentText(contentString);
            if ("tg".equals(menu.getTypeShort())) {
                if (!allContent.toString()
                               .isEmpty()) {
                    allContent.append('\n');
                }
                allContent.append(contentString);
            }
            if (firstContent.toString()
                            .isEmpty()) {
                firstContent.append(COMPILE.matcher(menu.getName())
                                           .replaceAll("")
                                           .trim())
                            .append('…');
            } else {
                morePageNotification.addPage(pageNotification.build());
            }
        }

        notificationBuilder.setWhen(mCafeteria.getNextMenuDate().getMillis());
        notificationBuilder.setContentText(firstContent);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(allContent));
        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.wear_cafeteria);
        morePageNotification.setBackground(bm);
        return morePageNotification.extend(notificationBuilder)
                                   .build();
    }

    @Override
    public RemoteViews getRemoteViews(Context context, int appWidgetId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.cards_widget_card);
        remoteViews.setTextViewText(R.id.widgetCardTextView, this.getTitle());
        remoteViews.setImageViewResource(R.id.widgetCardImageView, R.drawable.ic_cutlery);
        return remoteViews;
    }
}
