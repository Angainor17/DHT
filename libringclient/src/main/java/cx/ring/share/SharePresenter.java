package cx.ring.share;

import javax.inject.Inject;

import cx.ring.mvp.GenericView;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class SharePresenter extends RootPresenter<GenericView<ShareViewModel>> {
    private final AccountService mAccountService;
    private final Scheduler mUiScheduler;

    @Inject
    public SharePresenter(AccountService accountService, Scheduler uiScheduler) {
        mAccountService = accountService;
        mUiScheduler = uiScheduler;
    }

    @Override
    public void bindView(GenericView<ShareViewModel> view) {
        super.bindView(view);
        mCompositeDisposable.add(mAccountService
                .getCurrentAccountSubject()
                .map(ShareViewModel::new)
                .subscribeOn(Schedulers.computation())
                .observeOn(mUiScheduler)
                .subscribe(this::loadContactInformation));
    }

    private void loadContactInformation(ShareViewModel model) {
        GenericView<ShareViewModel> view = getView();
        if (view != null) {
            view.showViewModel(model);
        }
    }
}
