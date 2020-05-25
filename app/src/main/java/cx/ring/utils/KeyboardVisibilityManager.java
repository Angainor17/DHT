package cx.ring.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardVisibilityManager {
    public final static String TAG = KeyboardVisibilityManager.class.getSimpleName();

    public static void showKeyboard(final Activity activity,
                                    final View viewToFocus,
                                    final int tag) {
        if (null == activity) {
            Log.d(TAG, "showKeyboard: no activity");
            return;
        }

        if (null == viewToFocus) {
            Log.d(TAG, "showKeyboard: no viewToFocus");
            return;
        }

        Log.d(TAG, "showKeyboard: showing keyboard");
        viewToFocus.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @SuppressWarnings("unused")
    public static void hideKeyboard(final Activity activity,
                                    final int tag) {
        if (null == activity) {
            Log.d(TAG, "hideKeyboard: no activity");
            return;
        }

        final View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            Log.d(TAG, "hideKeyboard: hiding keyboard");
            currentFocus.clearFocus();
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), tag);
        }
    }
}
