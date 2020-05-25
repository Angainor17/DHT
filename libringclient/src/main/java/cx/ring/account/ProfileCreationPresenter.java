package cx.ring.account;

import javax.inject.Inject;

import cx.ring.mvp.AccountCreationModel;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HardwareService;
import cx.ring.utils.Log;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class ProfileCreationPresenter extends RootPresenter<ProfileCreationView> {

    public static final String TAG = ProfileCreationPresenter.class.getSimpleName();

    private final DeviceRuntimeService mDeviceRuntimeService;
    private final HardwareService mHardwareService;
    private final Scheduler mUiScheduler;

    private AccountCreationModel mAccountCreationModel;

    @Inject
    public ProfileCreationPresenter(DeviceRuntimeService deviceRuntimeService, HardwareService hardwareService, Scheduler uiScheduler) {
        mDeviceRuntimeService = deviceRuntimeService;
        mHardwareService = hardwareService;
        mUiScheduler = uiScheduler;
    }

    public void initPresenter(AccountCreationModel accountCreationModel) {
        Log.w(TAG, "initPresenter");
        mAccountCreationModel = accountCreationModel;
        if (mDeviceRuntimeService.hasContactPermission()) {
            String profileName = mDeviceRuntimeService.getProfileName();
            if (profileName != null) {
                getView().displayProfileName(profileName);
            }
        } else {
            Log.d(TAG, "READ_CONTACTS permission is not granted.");
        }
        mCompositeDisposable.add(accountCreationModel
                .getProfileUpdates()
                .observeOn(mUiScheduler)
                .subscribe(model -> {
                    ProfileCreationView view = getView();
                    if (view != null)
                        view.setProfile(model);
                }));
    }

    public void fullNameUpdated(String fullName) {
        if (mAccountCreationModel != null)
            mAccountCreationModel.setFullName(fullName);
    }

    public void photoUpdated(Single<Object> bitmap) {
        mCompositeDisposable.add(bitmap
                .subscribe(b -> mAccountCreationModel.setPhoto(b),
                        e -> Log.e(TAG, "Can't load image", e)));
    }

    public void galleryClick() {
        boolean hasPermission = mDeviceRuntimeService.hasGalleryPermission();
        if (hasPermission) {
            getView().goToGallery();
        } else {
            getView().askStoragePermission();
        }
    }

    public void cameraClick() {
        boolean hasPermission = mDeviceRuntimeService.hasVideoPermission() &&
                mDeviceRuntimeService.hasWriteExternalStoragePermission();
        if (hasPermission) {
            getView().goToPhotoCapture();
        } else {
            getView().askPhotoPermission();
        }
    }

    public void cameraPermissionChanged(boolean isGranted) {
        if (isGranted && mHardwareService.isVideoAvailable()) {
            mHardwareService.initVideo().subscribe();
        }
    }

    public void nextClick() {
        getView().goToNext(mAccountCreationModel, true);
    }

    public void skipClick() {
        getView().goToNext(mAccountCreationModel, false);
    }
}
