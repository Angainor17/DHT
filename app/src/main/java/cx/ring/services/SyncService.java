package cx.ring.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.client.HomeActivity;

public class SyncService extends Service {
    private static final String TAG = SyncService.class.getSimpleName();
    public static final int NOTIF_SYNC_SERVICE_ID = 1004;
    public static final String ACTION_START = "startService";
    public static final String ACTION_STOP = "stopService";

    private boolean isFirst = true;
    private final Random mRandom = new Random();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            if (isFirst) {
                isFirst = false;
                final Intent deleteIntent = new Intent(ACTION_STOP)
                        .setClass(getApplicationContext(), SyncService.class);
                final Intent contentIntent = new Intent(Intent.ACTION_VIEW)
                        .setClass(getApplicationContext(), HomeActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Notification notif = new NotificationCompat.Builder(this, NotificationServiceImpl.NOTIF_CHANNEL_SYNC)
                        .setContentTitle(getString(R.string.notif_sync_title))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setAutoCancel(false)
                        .setOngoing(false)
                        .setVibrate(null)
                        .setSmallIcon(R.drawable.ic_ring_logo_white)
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setOnlyAlertOnce(true)
                        .setDeleteIntent(PendingIntent.getService(getApplicationContext(), mRandom.nextInt(), deleteIntent, 0))
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), mRandom.nextInt(), contentIntent, 0))
                        .build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    startForeground(NOTIF_SYNC_SERVICE_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                else
                    startForeground(NOTIF_SYNC_SERVICE_ID, notif);

                JamiApplication.getInstance().startDaemon();
            }
        } else if (ACTION_STOP.equals(action)) {
            stopForeground(true);
            stopSelf();
            isFirst = true;
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}