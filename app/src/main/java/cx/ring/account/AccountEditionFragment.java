package cx.ring.account;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.client.HomeActivity;
import cx.ring.databinding.FragAccountSettingsBinding;
import cx.ring.fragments.AdvancedAccountFragment;
import cx.ring.fragments.GeneralAccountFragment;
import cx.ring.interfaces.BackHandlerInterface;
import cx.ring.mvp.BaseSupportFragment;

public class AccountEditionFragment extends BaseSupportFragment<AccountEditionPresenter> implements
        BackHandlerInterface,
        AccountEditionView,
        ViewTreeObserver.OnScrollChangedListener {
    private static final String TAG = AccountEditionFragment.class.getSimpleName();

    public static final String ACCOUNT_ID_KEY = AccountEditionFragment.class.getCanonicalName() + "accountid";
    static final String ACCOUNT_HAS_PASSWORD_KEY = AccountEditionFragment.class.getCanonicalName() + "hasPassword";
    public static final String ACCOUNT_ID = TAG + "accountID";

    private static final int SCROLL_DIRECTION_UP = -1;

    private FragAccountSettingsBinding binding;

    private boolean mIsVisible;

    private MenuItem mItemAdvanced;

    private String mAccountId;
    private boolean mAccountIsJami;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragAccountSettingsBinding.inflate(inflater, container, false);
        // dependency injection
        ((JamiApplication) getActivity().getApplication()).getInjectionComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);

        mAccountId = getArguments().getString(ACCOUNT_ID);
        binding.fragmentContainer.getViewTreeObserver().addOnScrollChangedListener(this);

        presenter.init(mAccountId);
    }

    @Override
    public void displaySummary(String accountId) {
        toggleView(accountId, true);
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(AccountSummaryFragment.TAG);
        if (existingFragment == null) {
            AccountSummaryFragment fragment = new AccountSummaryFragment();
            Bundle args = new Bundle();
            args.putString(ACCOUNT_ID_KEY, accountId);
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, AccountSummaryFragment.TAG)
                    .commit();
        } else {
            ((AccountSummaryFragment) existingFragment).setAccount(accountId);
        }
    }

    @Override
    public void initViewPager(String accountId) {
        binding.pager.setOffscreenPageLimit(4);
        binding.slidingTabs.setupWithViewPager(binding.pager);
        binding.pager.setAdapter(new PreferencesPagerAdapter(getChildFragmentManager(), getActivity(), accountId));
    }

    @Override
    public void showAdvancedOption(boolean show) {
        if (mItemAdvanced != null) {
            mItemAdvanced.setVisible(show);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.account_edition, menu);
        mItemAdvanced = menu.findItem(R.id.menuitem_advanced);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        presenter.prepareOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        setBackListenerEnabled(false);
    }

    public boolean onBackPressed() {
        mIsVisible = false;
        if (getActivity() instanceof HomeActivity)
            ((HomeActivity) getActivity()).setToolbarOutlineState(true);
        if (binding.fragmentContainer.getVisibility() != View.VISIBLE) {
            toggleView(mAccountId, mAccountIsJami);
            return true;
        }

        return getChildFragmentManager().popBackStackImmediate();
    }

    private void toggleView(String accountId, boolean isJami) {
        mAccountId = accountId;
        mAccountIsJami = isJami;
        binding.slidingTabs.setVisibility(isJami ? View.GONE : View.VISIBLE);
        binding.pager.setVisibility(isJami ? View.GONE : View.VISIBLE);
        binding.fragmentContainer.setVisibility(isJami ? View.VISIBLE : View.GONE);
        presenter.prepareOptionsMenu();
        setBackListenerEnabled(isJami);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getActivity() != null)
                    getActivity().onBackPressed();
                return true;
            case R.id.menuitem_delete:
                AlertDialog deleteDialog = createDeleteDialog();
                deleteDialog.show();
                break;
            case R.id.menuitem_advanced:
                binding.slidingTabs.setVisibility(View.VISIBLE);
                binding.pager.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                AccountSummaryFragment fragment = (AccountSummaryFragment) getChildFragmentManager().findFragmentByTag(AccountSummaryFragment.TAG);
                if (fragment != null)
                    fragment.setFragmentVisibility(false);
                mIsVisible = true;
                setupElevation();
                break;
            default:
                break;
        }
        return true;
    }

    @NonNull
    private AlertDialog createDeleteDialog() {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.account_delete_dialog_message)
                .setTitle(R.string.account_delete_dialog_title)
                .setPositiveButton(R.string.menu_delete, (dialog, whichButton) -> presenter.removeAccount())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        Activity activity = getActivity();
        if (activity != null)
            alertDialog.setOwnerActivity(getActivity());
        return alertDialog;
    }

    @Override
    public void exit() {
        Activity activity = getActivity();
        if (activity != null)
            activity.onBackPressed();
    }

    private void setBackListenerEnabled(boolean enable) {
        if (getActivity() instanceof HomeActivity)
            ((HomeActivity) getActivity()).setAccountFragmentOnBackPressedListener(enable ? this : null);
    }

    private static class PreferencesPagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private String accountId;

        PreferencesPagerAdapter(FragmentManager fm, Context mContext, String accountId) {
            super(fm);
            this.mContext = mContext;
            this.accountId = accountId;
        }

        @StringRes
        private static int getRingPanelTitle(int position) {
            switch (position) {
                case 0:
                    return R.string.account_preferences_basic_tab;
                case 1:
                    return R.string.account_preferences_advanced_tab;
                default:
                    return -1;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return getJamiPanel(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int resId = getRingPanelTitle(position);
            return mContext.getString(resId);
        }

        @NonNull
        private Fragment getJamiPanel(int position) {
            switch (position) {
                case 0:
                    return fragmentWithBundle(new GeneralAccountFragment());
                case 1:
                    return fragmentWithBundle(new AdvancedAccountFragment());
                default:
                    throw new IllegalArgumentException();
            }
        }

        private Fragment fragmentWithBundle(Fragment result) {
            Bundle args = new Bundle();
            args.putString(ACCOUNT_ID_KEY, accountId);
            result.setArguments(args);
            return result;
        }
    }

    @Override
    public void onScrollChanged() {
        setupElevation();
    }

    private void setupElevation() {
        if (binding == null || !mIsVisible) {
            return;
        }
        Activity activity = getActivity();
        if (!(activity instanceof HomeActivity))
            return;
        LinearLayout ll = (LinearLayout) binding.pager.getChildAt(binding.pager.getCurrentItem());
        if (ll == null) return;
        RecyclerView rv = (RecyclerView) ((FrameLayout) ll.getChildAt(0)).getChildAt(0);
        if (rv == null) return;
        HomeActivity homeActivity = (HomeActivity) activity;
        if (rv.canScrollVertically(SCROLL_DIRECTION_UP)) {
            binding.slidingTabs.setElevation(binding.slidingTabs.getResources().getDimension(R.dimen.toolbar_elevation));
            homeActivity.setToolbarElevation(true);
            homeActivity.setToolbarOutlineState(false);
        } else {
            binding.slidingTabs.setElevation(0);
            homeActivity.setToolbarElevation(false);
            homeActivity.setToolbarOutlineState(true);
        }
    }
}
