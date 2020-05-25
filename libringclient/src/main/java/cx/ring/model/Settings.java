package cx.ring.model;

public class Settings {
    private boolean mAllowPushNotifications;
    private boolean mAllowPersistentNotification;
    private boolean mAllowSystemContacts;
    private boolean mAllowPlaceSystemCalls;
    private boolean mAllowOnStartup;
    private boolean mHwEncoding;

    public Settings() {
    }

    public Settings(Settings s) {
        mAllowPushNotifications = s.mAllowPushNotifications;
        mAllowPersistentNotification = s.mAllowPersistentNotification;
        mAllowSystemContacts = s.mAllowSystemContacts;
        mAllowPlaceSystemCalls = s.mAllowPlaceSystemCalls;
        mAllowOnStartup = s.mAllowOnStartup;
        mHwEncoding = s.mHwEncoding;
    }

    public boolean isAllowPushNotifications() {
        return mAllowPushNotifications;
    }

    public void setAllowPushNotifications(boolean push) {
        this.mAllowPushNotifications = push;
    }

    public boolean isAllowSystemContacts() {
        return mAllowSystemContacts;
    }

    public void setAllowSystemContacts(boolean allowSystemContacts) {
        this.mAllowSystemContacts = allowSystemContacts;
    }

    public boolean isAllowPlaceSystemCalls() {
        return mAllowPlaceSystemCalls;
    }

    public void setAllowPlaceSystemCalls(boolean allowPlaceSystemCalls) {
        this.mAllowPlaceSystemCalls = allowPlaceSystemCalls;
    }

    public boolean isAllowOnStartup() {
        return mAllowOnStartup;
    }

    public void setAllowRingOnStartup(boolean allowRingOnStartup) {
        this.mAllowOnStartup = allowRingOnStartup;
    }

    public void setAllowPersistentNotification(boolean checked) {
        this.mAllowPersistentNotification = checked;
    }

    public boolean isAllowPersistentNotification() {
        return mAllowPersistentNotification;
    }
}
