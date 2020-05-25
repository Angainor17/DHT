package cx.ring.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import cx.ring.BuildConfig;

public class ClipboardHelper {
    public static final String TAG = ClipboardHelper.class.getSimpleName();
    public static final String COPY_CALL_CONTACT_NUMBER_CLIP_LABEL =
            BuildConfig.APPLICATION_ID + ".clipboard.contactNumber";

    public static void copyNumberToClipboard(final Activity activity,
                                             final String number,
                                             final ClipboardHelperCallback callback) {
        if (TextUtils.isEmpty(number)) {
            Log.d(TAG, "copyNumberToClipboard: number is null");
            return;
        }

        if (activity == null) {
            Log.d(TAG, "copyNumberToClipboard: activity is null");
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) activity
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText(COPY_CALL_CONTACT_NUMBER_CLIP_LABEL,
                number);
        clipboard.setPrimaryClip(clip);
        if (callback != null) {
            callback.clipBoardDidCopyNumber(number);
        }
    }

    public interface ClipboardHelperCallback {
        void clipBoardDidCopyNumber(String copiedNumber);
    }
}
