package cx.ring.account;

import javax.inject.Inject;

import cx.ring.mvp.AccountCreationModel;
import cx.ring.mvp.RootPresenter;
import cx.ring.utils.StringUtils;

public class AppAccountConnectPresenter extends RootPresenter<AppConnectAccountView> {

    private AccountCreationModel mAccountCreationModel;

    @Inject
    public AppAccountConnectPresenter() {
    }

    public void init(AccountCreationModel accountCreationModel) {
        mAccountCreationModel = accountCreationModel;
        if (mAccountCreationModel == null) {
            getView().cancel();
            return;
        }
        /*boolean hasArchive = mAccountCreationModel.getArchive() != null;
        JamiConnectAccountView view = getView();
        if (view != null) {
            view.showPin(!hasArchive);
            view.enableLinkButton(hasArchive);
        }*/
    }

    public void passwordChanged(String password) {
        mAccountCreationModel.setPassword(password);
        showConnectButton();
    }

    public void usernameChanged(String username) {
        mAccountCreationModel.setUsername(username);
        showConnectButton();
    }

    public void serverChanged(String server) {
        mAccountCreationModel.setManagementServer(server);
        showConnectButton();
    }

    public void connectClicked() {
        if (isFormValid()) {
            getView().createAccount(mAccountCreationModel);
        }
    }

    private void showConnectButton() {
        getView().enableConnectButton(isFormValid());
    }

    private boolean isFormValid() {
        return !StringUtils.isEmpty(mAccountCreationModel.getPassword())
                && !StringUtils.isEmpty(mAccountCreationModel.getUsername())
                && !StringUtils.isEmpty(mAccountCreationModel.getManagementServer());
    }

}
