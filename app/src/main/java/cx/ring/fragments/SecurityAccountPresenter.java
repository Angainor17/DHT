package cx.ring.fragments;

import java.util.List;

import javax.inject.Inject;

import cx.ring.model.Account;
import cx.ring.model.AccountCredentials;
import cx.ring.model.ConfigKey;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.utils.Tuple;

public class SecurityAccountPresenter extends RootPresenter<SecurityAccountView> {

    protected AccountService mAccountService;

    private Account mAccount;

    @Inject
    public SecurityAccountPresenter(AccountService accountService) {
        this.mAccountService = accountService;
    }

    public void init(String accountId) {
        mAccount = mAccountService.getAccount(accountId);
        if (mAccount != null) {
            getView().removeAllCredentials();
            getView().addAllCredentials(mAccount.getCredentials());

            List<String> methods = mAccountService.getTlsSupportedMethods();
            String[] tlsMethods = methods.toArray(new String[methods.size()]);

            getView().setDetails(mAccount.getConfig(), tlsMethods);
        }
    }

    public void credentialEdited(Tuple<AccountCredentials, AccountCredentials> result) {
        mAccount.removeCredential(result.first);
        if (result.second != null) {
            // There is a new value for this credentials it means it has been edited (otherwise deleted)
            mAccount.addCredential(result.second);
        }

        mAccountService.setCredentials(mAccount.getAccountID(), mAccount.getCredentialsHashMapList());
        mAccountService.setAccountDetails(mAccount.getAccountID(), mAccount.getDetails());

        getView().removeAllCredentials();
        getView().addAllCredentials(mAccount.getCredentials());
    }

    public void credentialAdded(Tuple<AccountCredentials, AccountCredentials> result) {
        mAccount.addCredential(result.second);

        mAccountService.setCredentials(mAccount.getAccountID(), mAccount.getCredentialsHashMapList());
        mAccountService.setAccountDetails(mAccount.getAccountID(), mAccount.getDetails());

        getView().removeAllCredentials();
        getView().addAllCredentials(mAccount.getCredentials());
    }

    public void tlsChanged(ConfigKey key, Object newValue) {
        if (newValue instanceof Boolean) {
            mAccount.setDetail(key, (Boolean) newValue);
        } else {
            mAccount.setDetail(key, (String) newValue);
        }

        mAccountService.setCredentials(mAccount.getAccountID(), mAccount.getCredentialsHashMapList());
        mAccountService.setAccountDetails(mAccount.getAccountID(), mAccount.getDetails());
    }

    public void fileActivityResult(ConfigKey key, String filePath) {
        mAccount.setDetail(key, filePath);
        mAccountService.setCredentials(mAccount.getAccountID(), mAccount.getCredentialsHashMapList());
        mAccountService.setAccountDetails(mAccount.getAccountID(), mAccount.getDetails());
    }
}
