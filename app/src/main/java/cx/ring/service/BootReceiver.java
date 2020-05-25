package cx.ring.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import javax.inject.Inject;

import cx.ring.application.JamiApplication;
import cx.ring.services.PreferencesService;
import cx.ring.services.SyncService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Inject
    PreferencesService mPreferencesService;

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_REBOOT.equals(action)) {
            try {
                ((JamiApplication) context.getApplicationContext()).getInjectionComponent().inject(this);
                if (mPreferencesService.getSettings().isAllowOnStartup()) {
                    try {
                        ContextCompat.startForegroundService(context, new Intent(SyncService.ACTION_START).setClass(context, SyncService.class));
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Error starting service", e);
                    }
                    new Handler().postDelayed(() -> {
                        try {
                            context.startService(new Intent(SyncService.ACTION_STOP).setClass(context, SyncService.class));
                        } catch (IllegalStateException ignored) {
                        }
                    }, 5 * DateUtils.SECOND_IN_MILLIS);
                }
            } catch (Exception e) {
                Log.e(TAG, "Can't start on boot", e);
            }
        }
    }
}
