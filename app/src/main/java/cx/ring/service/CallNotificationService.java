package cx.ring.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import javax.inject.Inject;

import cx.ring.application.JamiApplication;
import cx.ring.services.NotificationService;

public class CallNotificationService extends Service {

    private boolean isFirst = true;
    private static final int NOTIF_CALL_ID = 1001;
    private NotificationManagerCompat notificationManager;

    @Inject
    NotificationService mNotificationService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Notification notification = (Notification) mNotificationService.showCallNotification(intent.getIntExtra(NotificationService.KEY_NOTIFICATION_ID, -1));
        if (notification == null) {
            if (isFirst)
                stopSelf();
            return START_NOT_STICKY;
        }

        if (isFirst) {
            isFirst = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(NOTIF_CALL_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            else
                startForeground(NOTIF_CALL_ID, notification);
        } else {
            notificationManager.notify(NOTIF_CALL_ID, notification);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        ((JamiApplication) getApplication()).getInjectionComponent().inject(this);
        notificationManager = NotificationManagerCompat.from(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}