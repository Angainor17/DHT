package cx.ring.application;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.BuildConfig;
import cx.ring.contacts.AvatarFactory;
import cx.ring.daemon.Ringservice;
import cx.ring.dependencyinjection.AppInjectionComponent;
import cx.ring.dependencyinjection.AppInjectionModule;
import cx.ring.dependencyinjection.DaggerAppInjectionComponent;
import cx.ring.dependencyinjection.ServiceInjectionModule;
import cx.ring.facades.ConversationFacade;
import cx.ring.service.AppJobService;
import cx.ring.service.DRingService;
import cx.ring.services.AccountService;
import cx.ring.services.CallService;
import cx.ring.services.ContactService;
import cx.ring.services.DaemonService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HardwareService;
import cx.ring.services.PreferencesService;
import io.reactivex.plugins.RxJavaPlugins;

public abstract class JamiApplication extends Application {
    private static final String TAG = JamiApplication.class.getSimpleName();
    public static final String DRING_CONNECTION_CHANGED = BuildConfig.APPLICATION_ID + ".event.DRING_CONNECTION_CHANGE";
    public static final int PERMISSIONS_REQUEST = 57;
    private static final IntentFilter RINGER_FILTER = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
    private static JamiApplication sInstance = null;

    @Inject
    @Named("DaemonExecutor")
    ScheduledExecutorService mExecutor;
    @Inject
    DaemonService mDaemonService;
    @Inject
    AccountService mAccountService;
    @Inject
    CallService mCallService;
    @Inject
    HardwareService mHardwareService;
    @Inject
    PreferencesService mPreferencesService;
    @Inject
    DeviceRuntimeService mDeviceRuntimeService;
    @Inject
    ContactService mContactService;
    @Inject
    ConversationFacade mConversationFacade;

    private AppInjectionComponent appInjectionComponent;
    private final Map<String, Boolean> mPermissionsBeingAsked = new HashMap<>();
    private final BroadcastReceiver ringerModeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ringerModeChanged(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL));
        }
    };

    public abstract String getPushToken();

    private boolean mBound = false;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder s) {
            Log.d(TAG, "onServiceConnected: " + className.getClassName());
            mBound = true;
            // bootstrap Daemon
            //bootstrapDaemon();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected: " + className.getClassName());
            mBound = false;
        }
    };

    private void ringerModeChanged(int newMode) {
        boolean mute = newMode == AudioManager.RINGER_MODE_VIBRATE || newMode == AudioManager.RINGER_MODE_SILENT;
        mCallService.muteRingTone(mute);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AvatarFactory.clearCache();
        Glide.get(this).clearMemory();
    }

    public void bootstrapDaemon() {

        if (mDaemonService.isStarted()) {
            return;
        }

        mExecutor.execute(() -> {
            try {
                Log.d(TAG, "bootstrapDaemon: START");
                if (mDaemonService.isStarted()) {
                    return;
                }
                mDaemonService.startDaemon();

                // Check if the camera hardware feature is available.
                if (mDeviceRuntimeService.hasVideoPermission() && mHardwareService.isVideoAvailable()) {
                    //initVideo is called here to give time to the application to initialize hardware cameras
                    Log.d(TAG, "bootstrapDaemon: At least one camera available. Initializing video...");
                    mHardwareService.initVideo().subscribe();
                } else {
                    Log.d(TAG, "bootstrapDaemon: No camera available");
                }

                ringerModeChanged(((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode());
                registerReceiver(ringerModeListener, RINGER_FILTER);

                // load accounts from Daemon
                mAccountService.loadAccountsFromDaemon(mPreferencesService.hasNetworkConnected());

                if (mPreferencesService.getSettings().isAllowPushNotifications()) {
                    String token = getPushToken();
                    if (token != null) {
                        Ringservice.setPushNotificationToken(token);
                    }
                } else {
                    Ringservice.setPushNotificationToken("");
                }

                Intent intent = new Intent(DRING_CONNECTION_CHANGED);
                intent.putExtra("connected", mDaemonService.isStarted());
                sendBroadcast(intent);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    scheduleRefreshJob();
                }
            } catch (Exception e) {
                Log.e(TAG, "DRingService start failed", e);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleRefreshJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Log.e(TAG, "JobScheduler: can't retrieve service");
            return;
        }
        JobInfo.Builder jobBuilder = new JobInfo.Builder(AppJobService.JOB_ID, new ComponentName(this, AppJobService.class))
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            jobBuilder.setPeriodic(AppJobService.JOB_INTERVAL, AppJobService.JOB_FLEX);
        else
            jobBuilder.setPeriodic(AppJobService.JOB_INTERVAL);
        Log.w(TAG, "JobScheduler: scheduling job");
        scheduler.schedule(jobBuilder.build());
    }

    public void terminateDaemon() {
        Future<Boolean> stopResult = mExecutor.submit(() -> {
            unregisterReceiver(ringerModeListener);
            mDaemonService.stopDaemon();
            Intent intent = new Intent(DRING_CONNECTION_CHANGED);
            intent.putExtra("connected", mDaemonService.isStarted());
            sendBroadcast(intent);

            return true;
        });

        try {
            stopResult.get();
        } catch (Exception e) {
            Log.e(TAG, "DRingService stop failed", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        RxJavaPlugins.setErrorHandler(e -> Log.e(TAG, "Unhandled RxJava error", e));

        // building injection dependency tree
        appInjectionComponent = DaggerAppInjectionComponent.builder()
                .appInjectionModule(new AppInjectionModule(this))
                .serviceInjectionModule(new ServiceInjectionModule(this))
                .build();

        // we can now inject in our self whatever modules define
        appInjectionComponent.inject(this);

        bootstrapDaemon();

        mPreferencesService.loadDarkMode();
    }

    public void startDaemon() {
        if (!DRingService.isRunning) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        && mPreferencesService.getSettings().isAllowPersistentNotification()) {
                    startForegroundService(new Intent(this, DRingService.class));
                } else {
                    startService(new Intent(this, DRingService.class));
                }
            } catch (Exception e) {
                Log.w(TAG, "Error starting daemon service");
            }
        }
        bindDaemon();
    }

    public void bindDaemon() {
        if (!mBound) {
            try {
                bindService(new Intent(this, DRingService.class), mConnection, BIND_AUTO_CREATE | BIND_IMPORTANT | BIND_ABOVE_CLIENT);
            } catch (Exception e) {
                Log.w(TAG, "Error binding daemon service");
            }
        }
    }

    public static JamiApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // todo decide when to stop the daemon
        terminateDaemon();
        sInstance = null;
    }

    public AppInjectionComponent getInjectionComponent() {
        return appInjectionComponent;
    }

    public boolean canAskForPermission(String permission) {

        Boolean isBeingAsked = mPermissionsBeingAsked.get(permission);

        if (isBeingAsked != null && isBeingAsked) {
            return false;
        }

        mPermissionsBeingAsked.put(permission, true);

        return true;
    }

    public void permissionHasBeenAsked(String permission) {
        mPermissionsBeingAsked.remove(permission);
    }

    public DaemonService getDaemon() {
        return mDaemonService;
    }

    public HardwareService getHardwareService() {
        return mHardwareService;
    }
}
