package cx.ring.navigation;

import cx.ring.model.Account;

public interface HomeNavigationView {

    void showViewModel(HomeNavigationViewModel viewModel);

    void updateModel(Account account);

    void gotToImageCapture();

    void askCameraPermission();

    void goToGallery();

    void askGalleryPermission();

}
