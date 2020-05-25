package cx.ring.account;

import javax.inject.Inject;

import cx.ring.mvp.AccountCreationModel;
import cx.ring.mvp.RootPresenter;

public class AppLinkAccountPresenter extends RootPresenter<AppLinkAccountView> {

    private AccountCreationModel mAccountCreationModel;

    @Inject
    public AppLinkAccountPresenter() {
    }

    public void init(AccountCreationModel accountCreationModel) {
        mAccountCreationModel = accountCreationModel;
        if (mAccountCreationModel == null) {
            getView().cancel();
            return;
        }

        boolean hasArchive = mAccountCreationModel.getArchive() != null;
        AppLinkAccountView view = getView();
        if (view != null) {
            view.showPin(!hasArchive);
            view.enableLinkButton(hasArchive);
        }
    }

    public void passwordChanged(String password) {
        mAccountCreationModel.setPassword(password);
        showHideLinkButton();
    }

    public void pinChanged(String pin) {
        mAccountCreationModel.setPin(pin);
        showHideLinkButton();
    }

    public void linkClicked() {
        if (isFormValid()) {
            getView().createAccount(mAccountCreationModel);
        }
    }

    private void showHideLinkButton() {
        getView().enableLinkButton(isFormValid());
    }

    private boolean isFormValid() {
        return mAccountCreationModel.getArchive() != null || !mAccountCreationModel.getPin().isEmpty();
    }
}
