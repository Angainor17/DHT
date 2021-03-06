package cx.ring.account;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.model.Account;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HardwareService;
import cx.ring.utils.Log;
import cx.ring.utils.StringUtils;
import cx.ring.utils.VCardUtils;
import ezvcard.VCard;
import ezvcard.property.FormattedName;
import ezvcard.property.Photo;
import ezvcard.property.RawProperty;
import ezvcard.property.Uid;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class AppAccountSummaryPresenter extends RootPresenter<AppAccountSummaryView> {

    private static final String TAG = AppAccountSummaryPresenter.class.getSimpleName();

    private final DeviceRuntimeService mDeviceRuntimeService;
    private final HardwareService mHardwareService;
    private AccountService mAccountService;
    private String mAccountID;

    @Inject
    @Named("UiScheduler")
    protected Scheduler mUiScheduler;

    @Inject
    public AppAccountSummaryPresenter(AccountService accountService,
                                      HardwareService hardwareService,
                                      DeviceRuntimeService deviceRuntimeService) {
        mAccountService = accountService;
        mHardwareService = hardwareService;
        mDeviceRuntimeService = deviceRuntimeService;
    }

    public void registerName(String name, String password) {
        final Account account = mAccountService.getAccount(mAccountID);
        if (account == null || getView() == null) {
            return;
        }
        mAccountService.registerName(account, password, name);
        getView().accountChanged(account);
    }

    public void setAccountId(String accountID) {
        mCompositeDisposable.clear();
        mAccountID = accountID;
        AppAccountSummaryView v = getView();
        Account account = mAccountService.getAccount(mAccountID);
        if (v != null && account != null)
            v.accountChanged(account);
        mCompositeDisposable.add(mAccountService.getObservableAccountUpdates(mAccountID)
                .observeOn(mUiScheduler)
                .subscribe(a -> {
                    AppAccountSummaryView view = getView();
                    if (view != null)
                        view.accountChanged(a);
                }));
    }

    public void enableAccount(boolean newValue) {
        Account account = mAccountService.getAccount(mAccountID);
        if (account == null) {
            Log.w(TAG, "account not found!");
            return;
        }

        account.setEnabled(newValue);
        mAccountService.setAccountEnabled(account.getAccountID(), newValue);
    }

    public void revokeDevice(final String deviceId, String password) {
        if (getView() != null) {
            getView().showRevokingProgressDialog();
        }
        mCompositeDisposable.add(mAccountService
                .revokeDevice(mAccountID, password, deviceId)
                .observeOn(mUiScheduler)
                .subscribe(result -> getView().deviceRevocationEnded(deviceId, result)));
    }

    public void renameDevice(String newName) {
        mAccountService.renameDevice(mAccountID, newName);
    }

    public void changePassword(String oldPassword, String newPassword) {
        AppAccountSummaryView view = getView();
        if (view != null)
            view.showPasswordProgressDialog();
        mCompositeDisposable.add(mAccountService.setAccountPassword(mAccountID, oldPassword, newPassword)
                .observeOn(mUiScheduler)
                .subscribe(
                        () -> getView().passwordChangeEnded(true),
                        e -> getView().passwordChangeEnded(false)));
    }

    public String getDeviceName() {
        Account account = mAccountService.getAccount(mAccountID);
        if (account == null) {
            Log.w(TAG, "account not found!");
            return null;
        }
        return account.getDeviceName();
    }

    public void downloadAccountsArchive(File dest, String password) {
        getView().showExportingProgressDialog();
        mCompositeDisposable.add(
                mAccountService.exportToFile(mAccountID, dest.getAbsolutePath(), password)
                        .observeOn(mUiScheduler)
                        .subscribe(() -> getView().displayCompleteArchive(dest),
                                error -> getView().passwordChangeEnded(false)));
    }

    public void saveVCardFormattedName(String username) {
        Account account = mAccountService.getAccount(mAccountID);
        File filesDir = mDeviceRuntimeService.provideFilesDir();

        mCompositeDisposable.add(VCardUtils.loadLocalProfileFromDisk(filesDir, mAccountID)
                .doOnSuccess(vcard -> {
                    vcard.setFormattedName(username);
                    vcard.removeProperties(RawProperty.class);
                })
                .flatMap(vcard -> VCardUtils.saveLocalProfileToDisk(vcard, mAccountID, filesDir))
                .subscribeOn(Schedulers.io())
                .subscribe(vcard -> {
                    account.setProfile(vcard);
                    mAccountService.refreshAccounts();
                    getView().updateUserView(account);
                }, e -> Log.e(TAG, "Error saving vCard !", e)));
    }

    public void saveVCard(String username, Single<Photo> photo) {
        Account account = mAccountService.getAccount(mAccountID);
        String ringId = account.getUsername();
        File filesDir = mDeviceRuntimeService.provideFilesDir();
        mCompositeDisposable.add(Single.zip(
                VCardUtils.loadLocalProfileFromDisk(filesDir, mAccountID).subscribeOn(Schedulers.io()),
                photo, (vcard, pic) -> {
                    vcard.setUid(new Uid(ringId));
                    if (!StringUtils.isEmpty(username)) {
                        vcard.setFormattedName(username);
                    }
                    vcard.removeProperties(Photo.class);
                    vcard.addPhoto(pic);
                    vcard.removeProperties(RawProperty.class);
                    return vcard;
                })
                .flatMap(vcard -> VCardUtils.saveLocalProfileToDisk(vcard, mAccountID, filesDir))
                .subscribeOn(Schedulers.io())
                .subscribe(vcard -> {
                    account.setProfile(vcard);
                    mAccountService.refreshAccounts();
                    getView().updateUserView(account);
                }, e -> Log.e(TAG, "Error saving vCard !", e)));
    }

    public void cameraClicked() {
        boolean hasPermission = mDeviceRuntimeService.hasVideoPermission() &&
                mDeviceRuntimeService.hasWriteExternalStoragePermission();
        if (hasPermission) {
            getView().gotToImageCapture();
        } else {
            getView().askCameraPermission();
        }
    }

    public void galleryClicked() {
        boolean hasPermission = mDeviceRuntimeService.hasGalleryPermission();
        if (hasPermission) {
            getView().goToGallery();
        } else {
            getView().askGalleryPermission();
        }
    }

    public String getAlias(Account account) {
        if (account == null) {
            Log.e(TAG, "Not able to get alias");
            return null;
        }
        VCard vcard = account.getProfile();
        if (vcard != null) {
            FormattedName name = vcard.getFormattedName();
            if (name != null) {
                String name_value = name.getValue();
                if (name_value != null && !name_value.isEmpty()) {
                    return name_value;
                }
            }
        }
        return null;
    }

    public String getAccountAlias(Account account) {
        if (account == null) {
            cx.ring.utils.Log.e(TAG, "Not able to get account alias");
            return null;
        }
        String alias = getAlias(account);
        return (alias == null) ? account.getAlias() : alias;
    }


}
