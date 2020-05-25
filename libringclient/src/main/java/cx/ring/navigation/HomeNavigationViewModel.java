package cx.ring.navigation;

import java.util.List;

import cx.ring.model.Account;

public class HomeNavigationViewModel {
    final private Account mAccount;
    final private List<Account> mAccounts;

    public HomeNavigationViewModel(Account account, List<Account> accounts) {
        mAccount = account;
        mAccounts = accounts;
    }

    public Account getAccount() {
        return mAccount;
    }

    public List<Account> getAccounts() {
        return mAccounts;
    }
}
