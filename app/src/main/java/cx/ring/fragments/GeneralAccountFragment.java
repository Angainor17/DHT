package cx.ring.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.TwoStatePreference;

import cx.ring.R;
import cx.ring.account.AccountEditionFragment;
import cx.ring.application.JamiApplication;
import cx.ring.model.Account;
import cx.ring.model.AccountConfig;
import cx.ring.model.ConfigKey;
import cx.ring.mvp.BasePreferenceFragment;
import cx.ring.services.SharedPreferencesServiceImpl;
import cx.ring.utils.Log;
import cx.ring.views.EditTextIntegerPreference;
import cx.ring.views.EditTextPreferenceDialog;
import cx.ring.views.PasswordPreference;

public class GeneralAccountFragment extends BasePreferenceFragment<GeneralAccountPresenter> implements GeneralAccountView {

    private static final String TAG = GeneralAccountFragment.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";
    private final Preference.OnPreferenceChangeListener changeAccountStatusListener = (preference, newValue) -> {
        presenter.setEnabled((Boolean) newValue);
        return false;
    };
    private final Preference.OnPreferenceChangeListener changeBasicPreferenceListener = (preference, newValue) -> {
        Log.i(TAG, "Changing preference " + preference.getKey() + " to value:" + newValue);
        final ConfigKey key = ConfigKey.fromString(preference.getKey());
        if (preference instanceof TwoStatePreference) {
            presenter.twoStatePreferenceChanged(key, newValue);
        } else if (preference instanceof PasswordPreference) {
            StringBuilder tmp = new StringBuilder();
            for (int i = 0; i < ((String) newValue).length(); ++i) {
                tmp.append("*");
            }
            preference.setSummary(tmp.toString());
            presenter.passwordPreferenceChanged(key, newValue);
        } else if (key == ConfigKey.ACCOUNT_USERNAME) {
            presenter.userNameChanged(key, newValue);
            preference.setSummary((CharSequence) newValue);
        } else {
            preference.setSummary((CharSequence) newValue);
            presenter.preferenceChanged(key, newValue);
        }
        return true;
    };

    public static GeneralAccountFragment newInstance(@NonNull String accountId) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountEditionFragment.ACCOUNT_ID_KEY, accountId);
        GeneralAccountFragment generalAccountFragment = new GeneralAccountFragment();
        generalAccountFragment.setArguments(bundle);
        return generalAccountFragment;
    }

    @Override
    public void accountChanged(@NonNull Account account) {
        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesMode(Context.MODE_PRIVATE);
        pm.setSharedPreferencesName(SharedPreferencesServiceImpl.PREFS_ACCOUNT + account.getAccountID());

        setPreferenceDetails(account.getConfig());

        setPreferenceListener(account.getConfig(), changeBasicPreferenceListener);
    }

    @Override
    public void finish() {
        Activity activity = getActivity();
        if (activity != null)
            activity.onBackPressed();
    }

    private CharSequence getFileSizeSummary(int size, int maxSize) {
        if (size == 0) {
            return getText(R.string.account_accept_files_never);
        } else if (size == maxSize) {
            return getText(R.string.account_accept_files_always);
        } else {
            return Formatter.formatFileSize(requireContext(), size * 1000 * 1000);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        ((JamiApplication) requireActivity().getApplication()).getInjectionComponent().inject(this);
        super.onCreatePreferences(bundle, rootKey);

        Bundle args = getArguments();
        presenter.init(args == null ? null : args.getString(AccountEditionFragment.ACCOUNT_ID_KEY));

        SeekBarPreference filePref = findPreference("acceptIncomingFilesMaxSize");
        if (filePref != null) {
            filePref.setOnPreferenceChangeListener((p, v) -> {
                SeekBarPreference pref = (SeekBarPreference) p;
                p.setSummary(getFileSizeSummary((Integer) v, pref.getMax()));
                return true;
            });
            filePref.setSummary(getFileSizeSummary(filePref.getValue(), filePref.getMax()));
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        FragmentManager fragmentManager = requireFragmentManager();
        if (fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        if (preference instanceof EditTextIntegerPreference) {
            EditTextPreferenceDialog f = EditTextPreferenceDialog.newInstance(preference.getKey(), EditorInfo.TYPE_CLASS_NUMBER);
            f.setTargetFragment(this, 0);
            f.show(fragmentManager, DIALOG_FRAGMENT_TAG);
        } else if (preference instanceof PasswordPreference) {
            EditTextPreferenceDialog f = EditTextPreferenceDialog.newInstance(preference.getKey(), EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            f.setTargetFragment(this, 0);
            f.show(fragmentManager, DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void setPreferenceDetails(AccountConfig details) {
        for (ConfigKey confKey : details.getKeys()) {
            Preference pref = findPreference(confKey.key());
            if (pref == null) {
                continue;
            }
            if (!confKey.isTwoState()) {
                String val = details.get(confKey);
                ((EditTextPreference) pref).setText(val);
                if (pref instanceof PasswordPreference) {
                    StringBuilder tmp = new StringBuilder();
                    for (int i = 0; i < val.length(); ++i) {
                        tmp.append("*");
                    }
                    pref.setSummary(tmp.toString());
                } else {
                    pref.setSummary(val);
                }
            } else {
                ((TwoStatePreference) pref).setChecked(details.getBool(confKey));
            }
        }
    }

    private void setPreferenceListener(AccountConfig details, Preference.OnPreferenceChangeListener listener) {
        for (ConfigKey confKey : details.getKeys()) {
            Preference pref = findPreference(confKey.key());
            if (pref != null) {
                pref.setOnPreferenceChangeListener(listener);
            }
        }
    }

    @Override
    public void addJamiPreferences(String accountId) {
        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesMode(Context.MODE_PRIVATE);
        pm.setSharedPreferencesName(SharedPreferencesServiceImpl.PREFS_ACCOUNT + accountId);
        addPreferencesFromResource(R.xml.account_prefs_app);
    }

    @Override
    public void addSipPreferences() {
        addPreferencesFromResource(R.xml.account_general_prefs);
    }
}
