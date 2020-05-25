package cx.ring.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import cx.ring.application.JamiApplication;
import cx.ring.services.SyncService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JamiJobService extends JobService {
    private static final String TAG = JamiJobService.class.getName();

    public static final long JOB_INTERVAL = 120 * DateUtils.MINUTE_IN_MILLIS;
    public static final long JOB_FLEX = 20 * DateUtils.MINUTE_IN_MILLIS;
    public static final long JOB_DURATION = 15 * DateUtils.SECOND_IN_MILLIS;
    public static final int JOB_ID = 3905;

    @Override
    public boolean onStartJob(final JobParameters params) {
        if (params.getJobId() != JOB_ID)
            return false;
        Log.w(TAG, "onStartJob() " + params);
        try {
            JamiApplication.getInstance().startDaemon();
            Intent serviceIntent = new Intent(SyncService.ACTION_START).setClass(this, SyncService.class);
            try {
                ContextCompat.startForegroundService(this, serviceIntent);
            } catch (IllegalStateException e) {
                android.util.Log.e(TAG, "Error starting service", e);
            }
            new Handler().postDelayed(() -> {
                Log.w(TAG, "jobFinished() " + params);
                try {
                    startService(new Intent(SyncService.ACTION_STOP).setClass(this, SyncService.class));
                } catch (IllegalStateException ignored) {
                }
                jobFinished(params, false);
            }, JOB_DURATION);
        } catch (Exception e) {
            Log.e(TAG, "onStartJob failed", e);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "onStopJob() " + params);
        try {
            synchronized (this) {
                notify();
            }
            try {
                startService(new Intent(SyncService.ACTION_STOP).setClass(this, SyncService.class));
            } catch (IllegalStateException ignored) {
            }
        } catch (Exception e) {
            Log.e(TAG, "onStopJob failed", e);
        }
        return false;
    }
}
