package cx.ring.views;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.preference.EditTextPreferenceDialogFragmentCompat;

public class EditTextPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {
    private static final String ARG_TYPE = "inputType";

    public static EditTextPreferenceDialog newInstance(String key, int type) {
        final EditTextPreferenceDialog fragment = new EditTextPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        b.putInt(ARG_TYPE, type);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText text = view.findViewById(android.R.id.edit);
        text.setInputType(getArguments().getInt(ARG_TYPE));
    }
}
