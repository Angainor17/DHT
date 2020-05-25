package cx.ring.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class PasswordPreference extends EditTextPreference {

    public PasswordPreference(Context context) {
        this(context, null);
    }

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PasswordPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}