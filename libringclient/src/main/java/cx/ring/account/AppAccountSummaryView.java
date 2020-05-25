package cx.ring.account;

import java.io.File;

import cx.ring.model.Account;

public interface AppAccountSummaryView {

    void showExportingProgressDialog();

    void showRevokingProgressDialog();

    void showPasswordProgressDialog();

    void accountChanged(final Account account);

    void deviceRevocationEnded(String device, int status);

    void passwordChangeEnded(boolean ok);

    void displayCompleteArchive(File dest);

    void gotToImageCapture();

    void askCameraPermission();

    void goToGallery();

    void askGalleryPermission();

    void updateUserView(Account account);

}
