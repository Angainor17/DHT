package cx.ring.services;

import java.io.File;

public abstract class DeviceRuntimeService implements DaemonService.SystemInfoCallbacks {

    public abstract void loadNativeLibrary();

    public abstract File provideFilesDir();

    public abstract File getCacheDir();

    public abstract File getFilePath(String name);

    public abstract File getConversationPath(String conversationId, String name);

    public abstract File getTemporaryPath(String conversationId, String name);

    public abstract String getPushToken();

    public abstract boolean isConnectedMobile();

    public abstract boolean isConnectedEthernet();

    public abstract boolean isConnectedWifi();

    public abstract boolean isConnectedBluetooth();

    public abstract long provideDaemonThreadId();

    public abstract boolean hasVideoPermission();

    public abstract boolean hasAudioPermission();

    public abstract boolean hasContactPermission();

    public abstract boolean hasCallLogPermission();

    public abstract boolean hasGalleryPermission();

    public abstract boolean hasWriteExternalStoragePermission();

    public abstract String getProfileName();

}
