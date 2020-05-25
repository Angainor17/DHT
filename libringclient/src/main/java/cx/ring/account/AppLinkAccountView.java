package cx.ring.account;

import cx.ring.mvp.AccountCreationModel;

public interface AppLinkAccountView {

    void enableLinkButton(boolean enable);

    void showPin(boolean show);

    void createAccount(AccountCreationModel accountCreationModel);

    void cancel();
}
