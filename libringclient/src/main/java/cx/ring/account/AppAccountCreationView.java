package cx.ring.account;

import cx.ring.mvp.AccountCreationModel;

public interface AppAccountCreationView {

    enum UsernameAvailabilityStatus {
        ERROR_USERNAME_TAKEN,
        ERROR_USERNAME_INVALID,
        ERROR,
        LOADING,
        AVAILABLE,
        RESET
    }

    void updateUsernameAvailability(UsernameAvailabilityStatus status);

    void showInvalidPasswordError(boolean display);

    void showNonMatchingPasswordError(boolean display);

    void displayUsernameBox(boolean display);

    void enableNextButton(boolean enabled);

    void goToAccountCreation(AccountCreationModel accountCreationModel);

    void cancel();
}
