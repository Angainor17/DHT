package cx.ring.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import java.util.ArrayList;

import cx.ring.R;
import cx.ring.account.AccountEditionFragment;
import cx.ring.application.JamiApplication;
import cx.ring.model.AccountConfig;
import cx.ring.model.ConfigKey;
import cx.ring.mvp.BasePreferenceFragment;
import cx.ring.views.EditTextIntegerPreference;
import cx.ring.views.EditTextPreferenceDialog;
import cx.ring.views.PasswordPreference;

public class AdvancedAccountFragment extends BasePreferenceFragment<AdvancedAccountPresenter> implements AdvancedAccountView, Preference.OnPreferenceChangeListener {

    private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        ((JamiApplication) requireActivity().getApplication()).getInjectionComponent().inject(this);
        super.onCreatePreferences(bundle, s);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.account_advanced_prefs);

        Bundle args = getArguments();
        presenter.init(args == null ? null : args.getString(AccountEditionFragment.ACCOUNT_ID_KEY));
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

    @Override
    public void initView(AccountConfig config, ArrayList<CharSequence> networkInterfaces) {
        for (ConfigKey confKey : config.getKeys()) {
            Preference pref = findPreference(confKey.key());
            if (pref != null) {
                pref.setOnPreferenceChangeListener(this);
                if (confKey == ConfigKey.LOCAL_INTERFACE) {
                    String val = config.get(confKey);
                    CharSequence[] display = networkInterfaces.toArray(new CharSequence[networkInterfaces.size()]);
                    ListPreference listPref = (ListPreference) pref;
                    listPref.setEntries(display);
                    listPref.setEntryValues(display);
                    listPref.setSummary(val);
                    listPref.setValue(val);
                } else if (!confKey.isTwoState()) {
                    String val = config.get(confKey);
                    pref.setSummary(val);
                    if (pref instanceof EditTextPreference) {
                        ((EditTextPreference) pref).setText(val);
                    }
                } else {
                    ((TwoStatePreference) pref).setChecked(config.getBool(confKey));
                }
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ConfigKey key = ConfigKey.fromString(preference.getKey());

        presenter.preferenceChanged(key, newValue);
        if (preference instanceof TwoStatePreference) {
            presenter.twoStatePreferenceChanged(key, newValue);
        } else if (preference instanceof PasswordPreference) {
            presenter.passwordPreferenceChanged(key, newValue);
            preference.setSummary(TextUtils.isEmpty(newValue.toString()) ? "" : "******");
        } else {
            presenter.preferenceChanged(key, newValue);
            preference.setSummary(newValue.toString());
        }
        return true;
    }
}
