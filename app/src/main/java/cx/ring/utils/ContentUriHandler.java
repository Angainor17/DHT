package cx.ring.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

import cx.ring.BuildConfig;

/**
 * This class distributes content uri used to pass along data in the app
 */
public class ContentUriHandler {
    private final static String TAG = ContentUriHandler.class.getSimpleName();

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final String AUTHORITY_FILES = AUTHORITY + ".file_provider";

    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri CONVERSATION_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "conversation");
    public static final Uri ACCOUNTS_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "accounts");
    public static final Uri CONTACT_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "contact");

    private ContentUriHandler() {
        // hidden constructor
    }

    /**
     * The following is a workaround used to mitigate getUriForFile exceptions
     * on Huawei devices taken from stackoverflow
     * https://stackoverflow.com/a/41309223
     */
    private static final String HUAWEI_MANUFACTURER = "Huawei";

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Log.w(TAG, "Returning Uri.fromFile to avoid Huawei 'external-files-path' bug for pre-N devices", e);
                    return Uri.fromFile(file);
                } else {
                    Log.w(TAG, "ANR Risk -- Copying the file the location cache to avoid Huawei 'external-files-path' bug for N+ devices", e);
                    // Note: Periodically clear this cache
                    final File cacheFolder = new File(context.getCacheDir(), HUAWEI_MANUFACTURER);
                    final File cacheLocation = new File(cacheFolder, file.getName());
                    if (FileUtils.copyFile(file, cacheLocation)) {
                        Log.i(TAG, "Completed Android N+ Huawei file copy. Attempting to return the cached file");
                        return FileProvider.getUriForFile(context, authority, cacheLocation);
                    }
                    Log.e(TAG, "Failed to copy the Huawei file. Re-throwing exception");
                    throw new IllegalArgumentException("Huawei devices are unsupported for Android N");
                }
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }
}
