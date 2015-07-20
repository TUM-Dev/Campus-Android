package de.tum.in.tumcampus.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.MainActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.GCMAlert;
import de.tum.in.tumcampus.models.GCMNotification;
import de.tum.in.tumcampus.models.TUMCabeClient;

public class Alarm extends GenericNotification {

    private static final String pubKey = "AAAAB3NzaC1yc2EAAAADAQABAAACAQCrgH8QxtVqGs8rdAMcz298iqrPDq4xDehG0qJVvZuL/7RupNi2CQCr9K39WpJtoDszWpC8Rn9/PmiX/xa1aAjvHkXvSxYL2s+WEteUbbJkfI3e71kyALLzaiPcwif22/Bww8rxhVokidL+c5eCgjneod6iT3TEEKPmXX+EDKTeTJq5TI2FY/Bipquo6xvyiQHFNxSkFVFxzto5+lOzmtMGIgm47RQ+/ZEsVp9ZajrNrX+Xg100WoBxSE6QEkZLsY12swsC5OcHu+tlUj51zqt1J9huZq3HT+Qv37PvGbEye9vZEUo1VTOo3YgwaQaAUQsgFKXarm/iEp8qEy2g6B0jZ0+802m4vcuORJqg8cAxW9oFH0sIqRO6DIGNTi5+LV5/S31ntOl9xGqr3M5pKNgUrcLySzVuGF17IqKcW3U24kjjZ4aALIMaK8cJZfvUBwdxDjpm7MiP2Y+tO7QN4F52aBockTxhmYTxpN4qOKByLXsBCTLUP6BTE16KgPIxMrsESigtzD5JLKTJAm7VXmg/OmSF52G6nqxrH4/7KHEfEt/QhTWAZV8Dbv24MWeu36tZx9gCh2i3UOSMzmt4eJW6WilTg7+mfD3hcWZSEVuT1NvWiCPo8kP6Qt7z2X8m4dLdWore/SgqfKN98rv7oDrQFGq4uOXQcdN6/FvGOWIYdw==";
    public final GCMAlert alert;
    private GCMNotification info;

    public Alarm(String payload, Context context, int notification) {
        super(context, 3, notification, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new NullPointerException();
        }

        //Get data from server
        this.info = TUMCabeClient.getInstance(this.context).getNotification(this.notification);

        // parse data
        this.alert = (new Gson()).fromJson(payload, GCMAlert.class);
    }

    /**
     * Validates the signature sent to us
     *
     * @param title       the message title
     * @param description the message description
     * @param signature   the message signature
     * @return if the signature is valid
     */
    private static boolean isValidSignature(String title, String description, String signature) {
        String text = title + description;
        PublicKey key = getCabePublicKey();

        Signature sig;
        try {
            sig = Signature.getInstance("SHA1WithRSA");
        } catch (NoSuchAlgorithmException e) {
            Utils.log(e);
            return false;
        }

        try {
            sig.initVerify(key);
        } catch (InvalidKeyException e) {
            Utils.log(e);
            return false;
        }

        byte[] textBytes;
        try {
            textBytes = text.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            Utils.log(e);
            return false;
        }
        try {
            sig.update(textBytes);
        } catch (SignatureException e) {
            Utils.log(e);
            return false;
        }
        try {
            return sig.verify(Base64.decode(signature, Base64.DEFAULT));
        } catch (SignatureException e) {
            Utils.log(e);
            return false;
        }
    }

    /**
     * converts the above base64 representation of the public key to a java object
     *
     * @return the public key of the TUMCabe server
     */
    private static PublicKey getCabePublicKey() {
        // Base64 string -> Bytes
        byte[] keyBytes = Base64.decode(pubKey, Base64.DEFAULT);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            Utils.log(e);
            return null;
        }

        // Bytes -> PublicKey
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (InvalidKeySpecException e) {
            Utils.log(e);
            return null;
        }

    }

    @Override
    public Notification getNotification() {
        if (alert.silent || info == null) {
            //Do nothing
            return null;
        }

        if (!isValidSignature(info.getTitle(), info.getDescription(), info.getSignature())) {
            Utils.log("Received an invalid RSA signature");
            return null;
        }


        // GCMNotification sound
        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.message);
        Intent alarm = new Intent(this.context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this.context, 0, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(this.icon)
                .setContentTitle(info.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(info.getDescription()))
                .setContentText(info.getDescription())
                .setContentIntent(pending)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setLights(0xff0000ff, 500, 500)
                .setSound(sound)
                .setAutoCancel(true)
                .build();
    }

    @Override
    public int getNotificationIdentification() {
        return 3;
    }
}
