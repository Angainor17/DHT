package cx.ring.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

/**
 * Ringer manager
 */
public class Ringer {
    private static final String TAG = Ringer.class.getSimpleName();
    private static final long[] VIBRATE_PATTERN = {0, 1000, 1000};

    @SuppressLint("NewApi")
    private static final AudioAttributes VIBRATE_ATTRIBUTES = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
            new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build() : null;

    private final Context context;
    private final Vibrator vibrator;

    public Ringer(Context aContext) {
        context = aContext;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Starts the ringtone and/or vibrator.
     */
    public void ring() {
        Log.d(TAG, "ring: called...");

        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        vibrator.cancel();

        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            //No ring no vibrate
            Log.d(TAG, "ring: skipping ring and vibrate because profile is Silent");
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE || ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // Vibrate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                vibrator.vibrate(VIBRATE_PATTERN, 0, VIBRATE_ATTRIBUTES);
            } else {
                vibrator.vibrate(VIBRATE_PATTERN, 0);
            }
        }
    }

    /**
     * Stops the ringtone and/or vibrator if any of these are actually
     * ringing/vibrating.
     */
    public void stopRing() {
        Log.d(TAG, "stopRing: called...");
        vibrator.cancel();
    }

}
