package cx.ring.account;

import cx.ring.model.Account;
import cx.ring.mvp.AccountCreationModel;
import ezvcard.VCard;
import io.reactivex.Single;

public interface AccountWizardView {

    void goToAccCreation();

    void displayProgress(boolean display);

    void displayCreationError();

    void blockOrientation();

    void finish(boolean affinity);

    Single<VCard> saveProfile(Account account, AccountCreationModel accountCreationModel);

    void displayGenericError();

    void displayNetworkError();

    void displayCannotBeFoundError();

    void displaySuccessDialog();

    void goToProfileCreation(AccountCreationModel accountCreationModel);
}