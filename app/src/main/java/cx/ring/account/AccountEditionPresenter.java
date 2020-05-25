package cx.ring.account;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.model.Account;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import io.reactivex.Scheduler;

public class AccountEditionPresenter extends RootPresenter<AccountEditionView> {
    private final AccountService mAccountService;
    private final Scheduler mUiScheduler;

    private Account mAccount;

    @Inject
    public AccountEditionPresenter(AccountService accountService, @Named("UiScheduler") Scheduler uiScheduler) {
        mAccountService = accountService;
        mUiScheduler = uiScheduler;
    }

    public void init(String accountId) {
        init(mAccountService.getAccount(accountId));
        mCompositeDisposable.add(mAccountService
                .getCurrentAccountSubject()
                .observeOn(mUiScheduler)
                .subscribe(a -> {
                    if (mAccount != a) {
                        init(a);
                    }
                }));
    }

    public void init(Account account) {
        final AccountEditionView view = getView();
        if (account == null) {
            if (view != null)
                view.exit();
            return;
        }
        mAccount = account;

        view.displaySummary(account.getAccountID());
        view.initViewPager(account.getAccountID());
    }

    public void removeAccount() {
        mAccountService.removeAccount(mAccount.getAccountID());
    }

    public void prepareOptionsMenu() {
        if (mAccount != null) {
            AccountEditionView view = getView();
            if (view != null) {
                view.showAdvancedOption(true);
            }
        }
    }
}
