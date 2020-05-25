package cx.ring.account;

import javax.inject.Inject;

import cx.ring.mvp.RootPresenter;

public class HomeAccountCreationPresenter extends RootPresenter<HomeAccountCreationView> {

    @Inject
    public HomeAccountCreationPresenter() {
    }

    public void clickOnCreateAccount() {
        getView().goToAccountCreation();
    }

}
