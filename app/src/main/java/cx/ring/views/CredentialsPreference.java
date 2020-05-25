package cx.ring.views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import cx.ring.R;
import cx.ring.model.AccountCredentials;

public class CredentialsPreference extends DialogPreference {
    private AccountCredentials creds;

    public CredentialsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CredentialsPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public CredentialsPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.dialogPreferenceStyle);
    }

    public CredentialsPreference(Context context) {
        this(context, null);
    }

    public AccountCredentials getCreds() {
        return creds;
    }

    public void setCreds(AccountCredentials c) {
        creds = c;
        if (creds != null) {
            setTitle(creds.getUsername());
            setSummary(TextUtils.isEmpty(creds.getRealm()) ? "*" : creds.getRealm());
            setDialogTitle(R.string.account_credentials_edit);
            setPositiveButtonText(android.R.string.ok);
            setNegativeButtonText(android.R.string.cancel);
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.isPersistent()) {
            return superState;
        } else {
            CredentialsPreference.SavedState myState = new CredentialsPreference.SavedState(superState);
            myState.creds = getCreds();
            return myState;
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null && state.getClass().equals(CredentialsPreference.SavedState.class)) {
            CredentialsPreference.SavedState myState = (CredentialsPreference.SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            setCreds(myState.creds);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<CredentialsPreference.SavedState> CREATOR = new Creator<CredentialsPreference.SavedState>() {
            public CredentialsPreference.SavedState createFromParcel(Parcel in) {
                return new CredentialsPreference.SavedState(in);
            }

            public CredentialsPreference.SavedState[] newArray(int size) {
                return new CredentialsPreference.SavedState[size];
            }
        };
        AccountCredentials creds;

        public SavedState(Parcel source) {
            super(source);
            creds = (AccountCredentials) source.readSerializable();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(creds);
        }
    }
}
