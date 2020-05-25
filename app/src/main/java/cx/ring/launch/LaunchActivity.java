package cx.ring.launch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import cx.ring.R;
import cx.ring.account.AccountWizardActivity;
import cx.ring.application.JamiApplication;
import cx.ring.client.HomeActivity;
import cx.ring.mvp.BaseActivity;

public class LaunchActivity extends BaseActivity<LaunchPresenter> implements LaunchView {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // dependency injection
        JamiApplication.getInstance().getInjectionComponent().inject(this);
        super.onCreate(savedInstanceState);
        JamiApplication.getInstance().startDaemon();

        setContentView(R.layout.activity_launch);

        presenter.init();
    }

    @Override
    public void askPermissions(String[] permissionsWeCanAsk) {
        ActivityCompat.requestPermissions(this, permissionsWeCanAsk, JamiApplication.PERMISSIONS_REQUEST);
    }

    @Override
    public void goToHome() {
        startActivity(new Intent(LaunchActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void goToAccountCreation() {
        startActivity(new Intent(LaunchActivity.this, AccountWizardActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case JamiApplication.PERMISSIONS_REQUEST: {
                if (grantResults.length == 0) {
                    return;
                }
                for (int i = 0, n = permissions.length; i < n; i++) {
                    String permission = permissions[i];
                    JamiApplication.getInstance().permissionHasBeenAsked(permission);
                    switch (permission) {
                        case Manifest.permission.READ_CONTACTS:
                            presenter.contactPermissionChanged(grantResults[i] == PackageManager.PERMISSION_GRANTED);
                            break;
                    }
                }
                break;
            }

        }
    }
}