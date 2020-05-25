package cx.ring.client;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.BuildConfig;
import cx.ring.R;
import cx.ring.account.AccountEditionFragment;
import cx.ring.account.AccountWizardActivity;
import cx.ring.application.JamiApplication;
import cx.ring.contactrequests.ContactRequestsFragment;
import cx.ring.contacts.AvatarFactory;
import cx.ring.databinding.ActivityHomeBinding;
import cx.ring.fragments.SmartListFragment;
import cx.ring.interfaces.BackHandlerInterface;
import cx.ring.interfaces.Colorable;
import cx.ring.model.Account;
import cx.ring.services.AccountService;
import cx.ring.services.NotificationService;
import cx.ring.utils.ContentUriHandler;
import cx.ring.utils.ConversationPath;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        Spinner.OnItemSelectedListener, Colorable {
    static final String TAG = HomeActivity.class.getSimpleName();

    public static final int REQUEST_CODE_PHOTO = 5;
    public static final int REQUEST_CODE_GALLERY = 6;
    public static final int REQUEST_CODE_QR_CONVERSATION = 7;
    public static final int REQUEST_PERMISSION_CAMERA = 113;
    public static final int REQUEST_PERMISSION_READ_STORAGE = 114;

    private static final int NAVIGATION_CONTACT_REQUESTS = 0;
    private static final int NAVIGATION_CONVERSATIONS = 1;

    public static final String HOME_TAG = "Home";
    public static final String CONTACT_REQUESTS_TAG = "Trust request";
    public static final String ACCOUNTS_TAG = "Accounts";

    public static final String ACTION_PRESENT_TRUST_REQUEST_FRAGMENT = BuildConfig.APPLICATION_ID + "presentTrustRequestFragment";

    protected Fragment fContent;

    private ToolbarSpinnerAdapter mAccountAdapter;
    private BackHandlerInterface mAccountFragmentBackHandlerInterface;

    private ViewOutlineProvider mOutlineProvider;

    private int mOrientation;

    @Inject
    AccountService mAccountService;
    @Inject
    NotificationService mNotificationService;

    @Inject
    @Named("UiScheduler")
    protected Scheduler mUiScheduler;

    private ActivityHomeBinding binding;

    private String mAccountWithPendingrequests = null;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final CompositeDisposable mAccountCheckDisposable = new CompositeDisposable();

    private boolean conversationSelected = false;

    /* called before activity is killed, e.g. rotation */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("orientation", mOrientation);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mOrientation = savedInstanceState.getInt("orientation");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisposable.add(mAccountCheckDisposable);

        JamiApplication.getInstance().startDaemon();

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // dependency injection
        JamiApplication.getInstance().getInjectionComponent().inject(this);

        mOrientation = getResources().getConfiguration().orientation;

        setSupportActionBar(binding.mainToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("");
        }

        binding.navigationView.setOnNavigationItemSelectedListener(this);
        binding.navigationView.getMenu().getItem(NAVIGATION_CONVERSATIONS).setChecked(true);

        mOutlineProvider = binding.appBar.getOutlineProvider();
        binding.spinnerToolbar.setOnItemSelectedListener(this);

        // if app opened from notification display trust request fragment when mService will connected
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        String action = intent.getAction();
        if (ACTION_PRESENT_TRUST_REQUEST_FRAGMENT.equals(action)) {
            if (extra == null || extra.getString(ContactRequestsFragment.ACCOUNT_ID) == null) {
                return;
            }
            mAccountWithPendingrequests = extra.getString(ContactRequestsFragment.ACCOUNT_ID);
        } else if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            handleShareIntent(intent);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fContent = fragmentManager.findFragmentById(R.id.main_frame);
        if (fContent == null || Intent.ACTION_SEARCH.equals(action)) {
            fContent = new SmartListFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fContent, HOME_TAG)
                    .commitNow();
        } else if (fContent instanceof Refreshable) {
            ((Refreshable) fContent).refresh();
        }
        if (mAccountWithPendingrequests != null) {
            presentTrustRequestFragment(mAccountWithPendingrequests);
            mAccountWithPendingrequests = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fContent = null;
        mDisposable.dispose();
        binding = null;
    }

    private void handleShareIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                if (ConversationPath.fromBundle(extra) != null) {
                    intent.setClass(this, ConversationActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent);
        String action = intent.getAction();
        if (ACTION_PRESENT_TRUST_REQUEST_FRAGMENT.equals(action)) {
            Bundle extra = intent.getExtras();
            if (extra == null || extra.getString(ContactRequestsFragment.ACCOUNT_ID) == null) {
                return;
            }
            presentTrustRequestFragment(extra.getString(ContactRequestsFragment.ACCOUNT_ID));
        } else if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            handleShareIntent(intent);
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            if (fContent instanceof SmartListFragment) {
                ((SmartListFragment) fContent).handleIntent(intent);
            }
        }
    }

    public void setToolbarState(int titleRes) {
        setToolbarState(getString(titleRes), null);
    }

    public void setToolbarState(String title, String subtitle) {
        binding.mainToolbar.setLogo(null);
        binding.mainToolbar.setTitle(title);

        if (subtitle != null) {
            binding.mainToolbar.setSubtitle(subtitle);
        } else {
            binding.mainToolbar.setSubtitle(null);
        }
    }

    private void showProfileInfo() {
        binding.spinnerToolbar.setVisibility(View.VISIBLE);
        binding.mainToolbar.setTitle(null);
        binding.mainToolbar.setSubtitle(null);

        int targetSize = (int) (AvatarFactory.SIZE_AB * getResources().getDisplayMetrics().density);
        mDisposable.add(mAccountService.getCurrentAccountSubject()
                .switchMapSingle(account -> AvatarFactory.getBitmapAvatar(HomeActivity.this, account, targetSize)
                        .map(avatar -> new Pair<>(account, avatar)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(d -> binding.mainToolbar.setLogo(new BitmapDrawable(getResources(), d.second)),
                        e -> Log.e(TAG, "Error loading avatar", e)));
    }

    /* activity gets back to the foreground and user input */
    @Override
    protected void onResume() {
        super.onResume();
        mAccountCheckDisposable.clear();
        mAccountCheckDisposable.add(mAccountService.getObservableAccountList()
                .firstElement()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accounts -> {
                    if (accounts.isEmpty()) {
                        startActivity(new Intent(this, AccountWizardActivity.class));
                    }
                }));

        mDisposable.add(mAccountService.getProfileAccountList()
                .observeOn(mUiScheduler)
                .subscribe(accounts -> {
                    mAccountAdapter = new ToolbarSpinnerAdapter(HomeActivity.this, R.layout.item_toolbar_spinner, accounts);
                    binding.spinnerToolbar.setAdapter(mAccountAdapter);
                    showProfileInfo();
                }, e -> Log.e(TAG, "Error loading account list !", e)));

        mDisposable.add((mAccountService
                .getCurrentAccountSubject()
                .switchMap(Account::getUnreadPending)
                .observeOn(mUiScheduler)
                .subscribe(count -> setBadge(R.id.navigation_requests, count))));

        mDisposable.add((mAccountService
                .getCurrentAccountSubject()
                .switchMap(Account::getUnreadConversations)
                .observeOn(mUiScheduler)
                .subscribe(count -> setBadge(R.id.navigation_home, count))));

        int newOrientation = getResources().getConfiguration().orientation;
        if (mOrientation != newOrientation) {
            mOrientation = newOrientation;
            hideTabletToolbar();
        }
    }

    private void presentTrustRequestFragment(String accountID) {
        Bundle bundle = new Bundle();
        bundle.putString(ContactRequestsFragment.ACCOUNT_ID, accountID);
        mNotificationService.cancelTrustRequestNotification(accountID);
        if (fContent instanceof ContactRequestsFragment) {
            ((ContactRequestsFragment) fContent).presentForAccount(accountID);
            return;
        }
        fContent = new ContactRequestsFragment();
        fContent.setArguments(bundle);
        binding.navigationView.getMenu().getItem(NAVIGATION_CONTACT_REQUESTS).setChecked(true);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.main_frame, fContent, CONTACT_REQUESTS_TAG)
                .addToBackStack(CONTACT_REQUESTS_TAG).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        if (mAccountFragmentBackHandlerInterface != null && mAccountFragmentBackHandlerInterface.onBackPressed()) {
            return;
        }
        super.onBackPressed();
        fContent = getSupportFragmentManager().findFragmentById(R.id.main_frame);
        if (fContent instanceof SmartListFragment) {
            binding.navigationView.getMenu().getItem(NAVIGATION_CONVERSATIONS).setChecked(true);
            showProfileInfo();
            showToolbarSpinner();
        }
    }

    private void popCustomBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int entryCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < entryCount; ++i) {
            fragmentManager.popBackStack();
        }
        fContent = fragmentManager.findFragmentById(R.id.main_frame);
        hideTabletToolbar();
        setToolbarElevation(false);
    }

    @Override
    public void setColor(int color) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Account account = mAccountService.getCurrentAccount();
        if (account == null)
            return false;

        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.navigation_requests:
                if (fContent instanceof ContactRequestsFragment) {
                    ((ContactRequestsFragment) fContent).presentForAccount(account.getAccountID());
                    break;
                }
                popCustomBackStack();
                fContent = new ContactRequestsFragment();
                bundle.putString(ContactRequestsFragment.ACCOUNT_ID, account.getAccountID());
                fContent.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.main_frame, fContent, CONTACT_REQUESTS_TAG)
                        .setReorderingAllowed(true)
                        .addToBackStack(CONTACT_REQUESTS_TAG)
                        .commit();
                conversationSelected = false;
                showProfileInfo();
                showToolbarSpinner();
                break;
            case R.id.navigation_home:
                if (fContent instanceof SmartListFragment) {
                    break;
                }
                popCustomBackStack();
                fContent = new SmartListFragment();
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.main_frame, fContent, HOME_TAG)
                        .setReorderingAllowed(true)
                        .addToBackStack(HOME_TAG)
                        .commit();
                conversationSelected = false;
                showProfileInfo();
                showToolbarSpinner();
                break;
            case R.id.navigation_settings:

                if (account.needsMigration()) {
                    Log.d(TAG, "launchAccountMigrationActivity: Launch account migration activity");

                    Intent intent = new Intent()
                            .setClass(this, AccountWizardActivity.class)
                            .setData(Uri.withAppendedPath(ContentUriHandler.ACCOUNTS_CONTENT_URI, account.getAccountID()));
                    startActivityForResult(intent, 1);
                } else {
                    Log.d(TAG, "launchAccountEditFragment: Launch account edit fragment");
                    bundle.putString(AccountEditionFragment.ACCOUNT_ID, account.getAccountID());

                    if (fContent instanceof AccountEditionFragment) {
                        break;
                    }
                    popCustomBackStack();
                    fContent = new AccountEditionFragment();
                    fContent.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(getFragmentContainerId(), fContent, ACCOUNTS_TAG)
                            .addToBackStack(ACCOUNTS_TAG)
                            .commit();
                    conversationSelected = false;
                    showProfileInfo();
                    showToolbarSpinner();
                    break;
                }

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mAccountAdapter.getItemViewType(position) == ToolbarSpinnerAdapter.TYPE_ACCOUNT) {
            mAccountService.setCurrentAccount(mAccountAdapter.getItem(position));
        } else {
            Intent intent = new Intent(HomeActivity.this, AccountWizardActivity.class);
            startActivity(intent);

            Account account = mAccountService.getCurrentAccount();
            if (account != null) {
                binding.spinnerToolbar.setSelection(mAccountService.getAccountList().indexOf(account.getAccountID()));
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface Refreshable {
        void refresh();
    }

    public void setBadge(int menuId, int number) {
        if (number == 0)
            binding.navigationView.removeBadge(menuId);
        else
            binding.navigationView.getOrCreateBadge(menuId).setNumber(number);
    }

    private void hideTabletToolbar() {
        if (binding != null) {
            binding.contactTitle.setText(null);
            binding.contactSubtitle.setText(null);
            binding.contactImage.setImageDrawable(null);
            binding.tabletToolbar.setVisibility(View.GONE);
        }
    }

    private void showToolbarSpinner() {
        binding.spinnerToolbar.setVisibility(View.VISIBLE);
    }

    private void hideToolbarSpinner() {
        if (binding != null) {
            binding.spinnerToolbar.setVisibility(View.GONE);
        }
    }

    public boolean isConversationSelected() {
        return conversationSelected;
    }

    private int getFragmentContainerId() {
        return R.id.main_frame;
    }

    public void setAccountFragmentOnBackPressedListener(BackHandlerInterface backPressedListener) {
        mAccountFragmentBackHandlerInterface = backPressedListener;
    }

    public void setToolbarElevation(boolean enable) {
        if (binding != null)
            binding.appBar.setElevation(enable ? getResources().getDimension(R.dimen.toolbar_elevation) : 0);
    }

    public void setToolbarOutlineState(boolean enabled) {
        if (binding != null) {
            if (!enabled) {
                binding.appBar.setOutlineProvider(null);
            } else {
                binding.appBar.setOutlineProvider(mOutlineProvider);
            }
        }
    }

    public void selectNavigationItem(int id) {
        if (binding != null)
            binding.navigationView.setSelectedItemId(id);
    }

}
