package cx.ring.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.lang.ref.WeakReference;

import cx.ring.R;
import cx.ring.services.AccountService;

public class RegisteredNameTextWatcher implements TextWatcher {

    private WeakReference<TextInputLayout> mInputLayout;
    private WeakReference<EditText> mInputText;
    private NameLookupInputHandler mNameLookupInputHandler;
    private String mLookingForAvailability;

    public RegisteredNameTextWatcher(Context context, AccountService accountService, String accountId, TextInputLayout inputLayout, EditText inputText) {
        mInputLayout = new WeakReference<>(inputLayout);
        mInputText = new WeakReference<>(inputText);
        mLookingForAvailability = context.getString(R.string.looking_for_username_availability);
        mNameLookupInputHandler = new NameLookupInputHandler(accountService, accountId);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mInputText.get() != null) {
            mInputText.get().setError(null);
        }
    }

    @Override
    public void afterTextChanged(final Editable txt) {
        final String name = txt.toString();

        if (mInputLayout.get() == null || mInputText.get() == null) {
            return;
        }

        if (TextUtils.isEmpty(name)) {
            mInputLayout.get().setErrorEnabled(false);
            mInputLayout.get().setError(null);
        } else {
            mInputLayout.get().setErrorEnabled(true);
            mInputLayout.get().setError(mLookingForAvailability);
        }

        if (!TextUtils.isEmpty(name)) {
            mNameLookupInputHandler.enqueueNextLookup(name);
        }
    }
}