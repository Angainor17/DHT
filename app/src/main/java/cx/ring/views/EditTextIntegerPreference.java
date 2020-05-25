package cx.ring.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class EditTextIntegerPreference extends EditTextPreference {

    public EditTextIntegerPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditTextIntegerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextIntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextIntegerPreference(Context context) {
        super(context);
    }
}
