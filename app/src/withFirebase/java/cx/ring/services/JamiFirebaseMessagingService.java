package cx.ring.services;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import cx.ring.application.JamiApplication;
import cx.ring.application.JamiApplicationFirebase;

public class JamiFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            // Even if wakeLock is deprecated, without this part, some devices are blocking
            // during the call negotiation. So, re-add this code to avoid to block here.
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake:push");
            wl.setReferenceCounted(false);
            wl.acquire(20 * 1000);
        } catch (Exception e) {
            Log.w("JamiFirebaseMessaging", "Can't acquire wake lock", e);
        }

        JamiApplicationFirebase app = (JamiApplicationFirebase)JamiApplication.getInstance();
        if (app != null)
            app.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        JamiApplicationFirebase app = (JamiApplicationFirebase)JamiApplication.getInstance();
        if (app != null)
            app.setPushToken(refreshedToken);
    }
}
