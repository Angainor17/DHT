package cx.ring.account;

import cx.ring.mvp.AccountCreationModel;

public interface AppConnectAccountView {

    void enableConnectButton(boolean enable);

    void createAccount(AccountCreationModel accountCreationModel);

    void cancel();
}
