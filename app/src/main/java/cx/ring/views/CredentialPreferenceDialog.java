package cx.ring.views;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import cx.ring.R;
import cx.ring.model.AccountCredentials;

public class CredentialPreferenceDialog extends PreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_TEXT = "CredentialPreferenceDialog.creds";
    private EditText mUsernameField;
    private EditText mPasswordField;
    private EditText mRealmField;
    private AccountCredentials creds;

    public CredentialPreferenceDialog() {
    }

    public static CredentialPreferenceDialog newInstance(String key) {
        CredentialPreferenceDialog fragment = new CredentialPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            creds = this.getCredentialsPreference().getCreds();
        } else {
            creds = (AccountCredentials) savedInstanceState.getSerializable(SAVE_STATE_TEXT);
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_STATE_TEXT, creds);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.credentials_pref, null);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mUsernameField = view.findViewById(R.id.credentials_username);
        mPasswordField = view.findViewById(R.id.credentials_password);
        mRealmField = view.findViewById(R.id.credentials_realm);
        if (mUsernameField == null) {
            throw new IllegalStateException("Dialog view must contain an EditText with id @id/credentials_username");
        } else if (creds != null) {
            mUsernameField.setText(creds.getUsername());
            mPasswordField.setText(creds.getPassword());
            mRealmField.setText(creds.getRealm());
        }
    }

    private CredentialsPreference getCredentialsPreference() {
        return (CredentialsPreference) this.getPreference();
    }

    protected boolean needInputMethod() {
        return true;
    }

    public void onDialogClosed(boolean positiveResult) {
        AccountCredentials newcreds = new AccountCredentials(
                mUsernameField.getText().toString(),
                mPasswordField.getText().toString(),
                mRealmField.getText().toString());
        if (positiveResult) {
            if (this.getCredentialsPreference().callChangeListener(new Pair<>(creds, newcreds))) {
                this.getCredentialsPreference().setCreds(newcreds);
            }
        }
    }
}
