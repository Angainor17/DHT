package cx.ring.services;

import java.util.Set;

import javax.inject.Inject;

import cx.ring.model.Settings;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public abstract class PreferencesService {

    @Inject
    AccountService mAccountService;

    @Inject
    DeviceRuntimeService mDeviceService;

    private Settings mUserSettings;
    private final Subject<Settings> mSettingsSubject = BehaviorSubject.create();

    protected abstract Settings loadSettings();

    protected abstract void saveSettings(Settings settings);

    public Settings getSettings() {
        if (mUserSettings == null) {
            mUserSettings = loadSettings();
            mSettingsSubject.onNext(mUserSettings);
        }
        return new Settings(mUserSettings);
    }

    public void setSettings(Settings settings) {
        saveSettings(settings);
        boolean allowPush = settings.isAllowPushNotifications();
        if (mUserSettings == null || mUserSettings.isAllowPushNotifications() != allowPush) {
            mAccountService.setPushNotificationToken(allowPush ? mDeviceService.getPushToken() : "");
            mAccountService.setProxyEnabled(allowPush);
        }
        mUserSettings = settings;
        mSettingsSubject.onNext(settings);
    }

    protected Settings getUserSettings() {
        return mUserSettings;
    }

    public Observable<Settings> getSettingsSubject() {
        return mSettingsSubject;
    }

    public abstract boolean hasNetworkConnected();

    public abstract boolean isPushAllowed();

    public abstract void saveRequestPreferences(String accountId, String contactId);

    public abstract Set<String> loadRequestsPreferences(String accountId);

    public abstract void removeRequestPreferences(String accountId, String contactId);

    public abstract int getResolution();

    public abstract int getBitrate();

    public abstract boolean isHardwareAccelerationEnabled();


    public abstract void setDarkMode(boolean enabled);

    public abstract boolean getDarkMode();

    public abstract void loadDarkMode();

    public abstract int getMaxFileAutoAccept(String accountId);
}
