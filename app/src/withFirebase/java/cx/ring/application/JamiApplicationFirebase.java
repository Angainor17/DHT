package cx.ring.application;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

public class JamiApplicationFirebase extends JamiApplication {
    static private String TAG = JamiApplicationFirebase.class.getSimpleName();
    private String pushToken = null;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(c -> {
                Log.w(TAG, "Found push token");
                try {
                    setPushToken(c.getResult().getToken());
                } catch (Exception e) {
                    Log.e(TAG, "Can't set push token", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Can't start service", e);
        }
    }

    @Override
    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String token) {
        // Log.d(TAG, "setPushToken: " + token);
        pushToken = token;
        if (mAccountService != null && mPreferencesService != null) {
            if (mPreferencesService.getSettings().isAllowPushNotifications()) {
                mAccountService.setPushNotificationToken(token);
            }
        }
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Log.d(TAG, "onMessageReceived: " + remoteMessage.getFrom());
        if (mAccountService != null)
            mAccountService.pushNotificationReceived(remoteMessage.getFrom(), remoteMessage.getData());
    }
}
