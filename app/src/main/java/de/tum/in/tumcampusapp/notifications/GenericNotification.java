package de.tum.in.tumcampusapp.notifications;

import android.content.Context;

import java.io.IOException;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;

public abstract class GenericNotification {

    protected final int type;
    protected final int notification;
    protected final int icon = R.drawable.tum_logo;
    protected final boolean confirmation;

    protected final Context context;

    public GenericNotification(Context context, int type, int notification, boolean confirmation) {
        this.notification = notification;
        this.context = context;
        this.confirmation = confirmation;
        this.type = type;
    }

    public void sendConfirmation() throws IOException {
        //Legacy support: notification id is -1 when old gcm messages arrive
        if (!this.confirmation || this.notification == -1) {
            return;
        }
        Utils.logv("Confirmed notification " + this.notification);
        TUMCabeClient.getInstance(this.context)
                     .confirm(this.notification);
    }

    public abstract android.app.Notification getNotification();

    public abstract int getNotificationIdentification();

}
