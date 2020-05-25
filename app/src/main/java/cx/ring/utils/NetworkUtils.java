package cx.ring.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public final class NetworkUtils {
    /**
     * Get the network info
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        if (context == null)
            return null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return null;
        for (Network n : cm.getAllNetworks()) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(n);
            if (caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                continue;
            NetworkInfo nInfo = cm.getNetworkInfo(n);
            if (nInfo != null && nInfo.isConnected())
                return nInfo;
        }
        return null;
    }

    public static boolean isConnectivityAllowed(Context context) {
        NetworkInfo info = NetworkUtils.getNetworkInfo(context);
        return info != null && info.isConnected();
    }

    public static boolean isPushAllowed(Context context, boolean allowMobile) {
        if (allowMobile)
            return true;
        NetworkInfo info = NetworkUtils.getNetworkInfo(context);
        return info != null && info.getType() != ConnectivityManager.TYPE_MOBILE;
    }
}
