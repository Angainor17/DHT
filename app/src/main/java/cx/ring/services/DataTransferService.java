package cx.ring.services;

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
import cx.ring.utils.Log;

public class DataTransferService extends Service {
    private final String TAG = DataTransferService.class.getSimpleName();

    @Inject
    NotificationService mNotificationService;
    private NotificationManagerCompat notificationManager;

    private boolean isFirst = true;
    private static final int NOTIF_FILE_SERVICE_ID = 1002;
    private int serviceNotificationId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        int notificationId = intent.getIntExtra(NotificationService.KEY_NOTIFICATION_ID, -1);
        Notification notification = (Notification) mNotificationService.getDataTransferNotification(notificationId);

        if (notification == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (isFirst) {
            isFirst = false;
            mNotificationService.cancelFileNotification(notificationId, true);
            serviceNotificationId = notificationId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(NOTIF_FILE_SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            else
                startForeground(NOTIF_FILE_SERVICE_ID, notification);
        }

        if (mNotificationService.getDataTransferNotification(serviceNotificationId) == null) {
            mNotificationService.cancelFileNotification(notificationId, true);
            serviceNotificationId = notificationId;
        }

        if (notificationId == serviceNotificationId)
            notificationManager.notify(NOTIF_FILE_SERVICE_ID, notification);
        else
            notificationManager.notify(notificationId, notification);


        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate(), Service has been initialized");
        ((JamiApplication) getApplication()).getInjectionComponent().inject(this);
        notificationManager = NotificationManagerCompat.from(this);
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy(), Service has been destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}