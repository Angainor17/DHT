package cx.ring.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cx.ring.R;
import cx.ring.model.Uri;

public class OutgoingCallHandler extends BroadcastReceiver {
    public static final String KEY_CACHE_HAVE_RINGACCOUNT = "cache_haveRingAccount";
    public static final String KEY_CACHE_HAVE_SIPACCOUNT = "cache_haveSipAccount";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction()))
            return;

        String phoneNumber = getResultData();
        if (phoneNumber == null)
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean systemDialer = sharedPreferences.getBoolean(context.getString(R.string.pref_systemDialer_key), false);
        if (systemDialer) {
            boolean systemDialerSip = sharedPreferences.getBoolean(KEY_CACHE_HAVE_SIPACCOUNT, false);
            boolean systemDialerRing = sharedPreferences.getBoolean(KEY_CACHE_HAVE_RINGACCOUNT, false);

            Uri uri = new Uri(phoneNumber);
            boolean isRingId = uri.isRingId();
            if ((!isRingId && systemDialerSip) || (isRingId && systemDialerRing) || uri.isSingleIp()) {
                setResultData(null);
            }
        }
    }
}
