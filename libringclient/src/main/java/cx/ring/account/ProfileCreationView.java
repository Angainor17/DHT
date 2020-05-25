package cx.ring.account;

import cx.ring.mvp.AccountCreationModel;

public interface ProfileCreationView {

    void displayProfileName(String profileName);

    void goToGallery();

    void goToPhotoCapture();

    void askStoragePermission();

    void askPhotoPermission();

    void goToNext(AccountCreationModel accountCreationModel, boolean saveProfile);

    void setProfile(AccountCreationModel model);
}
