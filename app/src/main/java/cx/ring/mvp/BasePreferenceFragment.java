package cx.ring.mvp;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import javax.inject.Inject;

public abstract class BasePreferenceFragment<T extends RootPresenter> extends PreferenceFragmentCompat {
    @Inject
    protected T presenter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //Be sure to do the injection in onCreateView method
        presenter.bindView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unbindView();
    }

}
