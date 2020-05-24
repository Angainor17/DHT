/*
 *  Copyright (C) 2004-2019 Savoir-faire Linux Inc.
 *
 *  Author: Alexandre Lision <alexandre.lision@savoirfairelinux.com>
 *  Author: Adrien Béraud <adrien.beraud@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package cx.ring.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.client.HomeActivity;

import cx.ring.model.Account;
import cx.ring.model.AccountConfig;
import cx.ring.mvp.AccountCreationModel;
import cx.ring.mvp.BaseActivity;
import cx.ring.utils.VCardUtils;
import ezvcard.VCard;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class AccountWizardActivity extends BaseActivity<AccountWizardPresenter> implements AccountWizardView {

    private ProgressDialog mProgress = null;
    private String mAccountType;
    private AlertDialog mAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // dependency injection
        JamiApplication.getInstance().getInjectionComponent().inject(this);
        super.onCreate(savedInstanceState);

        JamiApplication.getInstance().startDaemon();
        setContentView(R.layout.activity_wizard);

        Intent intent = getIntent();
        if (intent != null) {
            mAccountType = intent.getAction();
        }

        if (mAccountType == null) {
            mAccountType = AccountConfig.ACCOUNT_TYPE_RING;
        }

        presenter.init(getIntent().getAction() != null ? getIntent().getAction() : AccountConfig.ACCOUNT_TYPE_RING);
    }

    @Override
    public void onDestroy() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.setOnDismissListener(null);
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public Single<VCard> saveProfile(final Account account, final AccountCreationModel accountCreationModel) {
        File filedir = getFilesDir();
        return accountCreationModel.toVCard()
                .flatMap(vcard -> {
                    account.setProfile(vcard);
                    return VCardUtils.saveLocalProfileToDisk(vcard, account.getAccountID(), filedir);
                })
                .subscribeOn(Schedulers.io());
    }

    public void createAccount(AccountCreationModel accountCreationModel) {
        if (!TextUtils.isEmpty(accountCreationModel.getManagementServer())) {
            presenter.initAccountConnect(accountCreationModel,
                    getText(R.string.ring_account_default_name).toString());
        } else if (accountCreationModel.isLink()) {
            presenter.initAccountLink(accountCreationModel,
                    getText(R.string.ring_account_default_name).toString());
        } else {
            presenter.initRingAccountCreation(accountCreationModel,
                    getText(R.string.ring_account_default_name).toString());
        }
    }

    @Override
    public void goToAccCreation() {
        Fragment fragment = AccountCreationFragment.newInstance(new AccountCreationModelImpl());
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.wizard_container, fragment, AccountCreationFragment.class.getSimpleName())
                .commit();
    }

    @Override
    public void goToProfileCreation(AccountCreationModel model) {
        Fragment fragment = ProfileCreationFragment.newInstance((AccountCreationModelImpl) model);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.wizard_container, fragment, ProfileCreationFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.wizard_container);
        if (fragment instanceof ProfileCreationFragment)
            finish();
        else
            super.onBackPressed();
    }

    @Override
    public void displayProgress(final boolean display) {
        if (display) {
            mProgress = new ProgressDialog(AccountWizardActivity.this);
            mProgress.setTitle(R.string.dialog_wait_create);
            mProgress.setMessage(getString(R.string.dialog_wait_create_details));
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
        } else {
            if (mProgress != null) {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
                mProgress = null;
            }
        }
    }

    @Override
    public void displayCreationError() {
        Toast.makeText(AccountWizardActivity.this, "Error creating account", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void blockOrientation() {
        //orientation is locked during the create of account to avoid the destruction of the thread
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    @Override
    public void finish(final boolean affinity) {
        if (affinity) {
            startActivity(new Intent(AccountWizardActivity.this, HomeActivity.class));
            finish();
        } else {
            finishAffinity();
        }
    }

    @Override
    public void displayGenericError() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            return;
        }
        mAlertDialog = new MaterialAlertDialogBuilder(AccountWizardActivity.this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.account_cannot_be_found_title)
                .setMessage(R.string.account_export_end_decryption_message)
                .show();
    }

    @Override
    public void displayNetworkError() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            return;
        }
        mAlertDialog = new MaterialAlertDialogBuilder(AccountWizardActivity.this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.account_no_network_title)
                .setMessage(R.string.account_no_network_message)
                .show();
    }

    @Override
    public void displayCannotBeFoundError() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            return;
        }
        mAlertDialog = new MaterialAlertDialogBuilder(AccountWizardActivity.this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.account_cannot_be_found_title)
                .setMessage(R.string.account_cannot_be_found_message)
                .setOnDismissListener(dialogInterface -> getSupportFragmentManager().popBackStack())
                .show();
    }

    @Override
    public void displaySuccessDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            return;
        }
        setResult(Activity.RESULT_OK, new Intent());
        //unlock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        presenter.successDialogClosed();
    }

    public void profileCreated(AccountCreationModel accountCreationModel, boolean saveProfile) {
        presenter.profileCreated(accountCreationModel, saveProfile);
    }
}
