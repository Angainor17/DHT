package cx.ring.launch;

import android.Manifest;

import java.util.ArrayList;

import javax.inject.Inject;

import cx.ring.model.Settings;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HardwareService;
import cx.ring.services.PreferencesService;

public class LaunchPresenter extends RootPresenter<LaunchView> {
    protected static final String TAG = LaunchPresenter.class.getSimpleName();

    protected AccountService mAccountService;
    protected DeviceRuntimeService mDeviceRuntimeService;
    protected PreferencesService mPreferencesService;
    protected HardwareService mHardwareService;

    @Inject
    public LaunchPresenter(AccountService accountService, DeviceRuntimeService deviceRuntimeService,
                           PreferencesService preferencesService, HardwareService hardwareService) {
        this.mAccountService = accountService;
        this.mDeviceRuntimeService = deviceRuntimeService;
        this.mPreferencesService = preferencesService;
        this.mHardwareService = hardwareService;
    }

    public void init() {
        String[] toRequest = buildPermissionsToAsk();
        if (toRequest.length == 0) {
            checkAccounts();
        } else {
            getView().askPermissions(toRequest);
        }
    }


    public void contactPermissionChanged(boolean isGranted) {
    }


    public void checkAccounts() {
//        mCompositeDisposable.add(mAccountService.getObservableAccountList()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(accounts -> {
//                            if (accounts.isEmpty()) {
        getView().goToAccountCreation();
//                            } else {
//                                getView().goToHome();
//                            }
//                            mCompositeDisposable.clear();
//                        },
//
//                        th -> {
//                            Log.d("", "");
//                        }
//                ));
    }

    private String[] buildPermissionsToAsk() {
        ArrayList<String> perms = new ArrayList<>();


        Settings settings = mPreferencesService.getSettings();

        if (settings.isAllowSystemContacts() && !mDeviceRuntimeService.hasContactPermission()) {
            perms.add(Manifest.permission.READ_CONTACTS);
        }


        if (settings.isAllowPlaceSystemCalls() && !mDeviceRuntimeService.hasCallLogPermission()) {
            perms.add(Manifest.permission.WRITE_CALL_LOG);
        }

        return perms.toArray(new String[perms.size()]);
    }
}