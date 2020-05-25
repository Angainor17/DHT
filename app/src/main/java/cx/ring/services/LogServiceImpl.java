package cx.ring.services;

import android.util.Log;

public class LogServiceImpl implements LogService {

    public void e(String tag, String message) {
        Log.e(tag, message);
    }

    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    public void i(String tag, String message) {
        Log.i(tag, message);
    }

    public void e(String tag, String message, Throwable e) {
        Log.e(tag, message, e);
    }

    public void d(String tag, String message, Throwable e) {
        Log.d(tag, message, e);
    }

    public void w(String tag, String message, Throwable e) {
        Log.w(tag, message, e);
    }

    public void i(String tag, String message, Throwable e) {
        Log.i(tag, message, e);
    }


}
