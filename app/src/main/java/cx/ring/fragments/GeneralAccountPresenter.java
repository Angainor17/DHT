package cx.ring.fragments;

import android.util.Log;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.model.Account;
import cx.ring.model.ConfigKey;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import io.reactivex.Scheduler;

public class GeneralAccountPresenter extends RootPresenter<GeneralAccountView> {

    private static final String TAG = GeneralAccountPresenter.class.getSimpleName();

    protected AccountService mAccountService;

    private Account mAccount;
    @Inject
    @Named("UiScheduler")
    protected Scheduler mUiScheduler;

    @Inject
    GeneralAccountPresenter(AccountService accountService) {
        this.mAccountService = accountService;
    }

    // Init with current account
    public void init() {
        init(mAccountService.getCurrentAccount());
    }

    public void init(String accountId) {
        init(mAccountService.getAccount(accountId));
    }

    private void init(Account account) {
        mAccount = account;
        if (account != null) {
            getView().addJamiPreferences(account.getAccountID());
            getView().accountChanged(account);
            mCompositeDisposable.clear();
            mCompositeDisposable.add(mAccountService.getObservableAccount(account.getAccountID())
                    .observeOn(mUiScheduler)
                    .subscribe(acc -> getView().accountChanged(acc)));
        } else {
            Log.e(TAG, "init: No currentAccount available");
            getView().finish();
        }
    }

    void setEnabled(boolean enabled) {
        mAccount.setEnabled(enabled);
        mAccountService.setAccountEnabled(mAccount.getAccountID(), enabled);
    }

    public void twoStatePreferenceChanged(ConfigKey configKey, Object newValue) {
        if (configKey == ConfigKey.ACCOUNT_ENABLE) {
            setEnabled((Boolean) newValue);
        } else {
            mAccount.setDetail(configKey, newValue.toString());
            updateAccount();
        }
    }

    void passwordPreferenceChanged(ConfigKey configKey, Object newValue) {
        updateAccount();
    }

    void userNameChanged(ConfigKey configKey, Object newValue) {
        updateAccount();
    }

    void preferenceChanged(ConfigKey configKey, Object newValue) {
        mAccount.setDetail(configKey, newValue.toString());
        updateAccount();
    }

    private void updateAccount() {
        mAccountService.setCredentials(mAccount.getAccountID(), mAccount.getCredentialsHashMapList());
        mAccountService.setAccountDetails(mAccount.getAccountID(), mAccount.getDetails());
    }
}
