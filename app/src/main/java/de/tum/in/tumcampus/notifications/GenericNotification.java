package de.tum.in.tumcampus.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import com.google.gson.Gson;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.ChatActivity;
import de.tum.in.tumcampus.activities.ChatRoomsActivity;
import de.tum.in.tumcampus.activities.MainActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.GCMChat;
import de.tum.in.tumcampus.models.TUMCabeClient;
import de.tum.in.tumcampus.models.managers.CardManager;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

public abstract class GenericNotification {

    protected final int type;
    protected final int notification;
    protected final int icon = R.drawable.tum_logo_notification;
    protected final boolean confirmation;

    protected final Context context;

    public GenericNotification(Context context, int type, int notification, boolean confirmation) {
        this.notification = notification;
        this.context = context;
        this.confirmation = confirmation;
        this.type=type;
    }

    public void sendConfirmation() {
        //Legacy support: notification id is -1 when old gcm messages arrive
        if (!this.confirmation || this.notification == -1) {
            return;
        }
        TUMCabeClient.getInstance(this.context).confirm(this.notification);
    }

    public abstract android.app.Notification getNotification();

    public abstract int getNotificationIdentification();


}
