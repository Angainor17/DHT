package cx.ring.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import cx.ring.R;
import cx.ring.adapters.SmartListAdapter;
import cx.ring.application.JamiApplication;
import cx.ring.client.ConversationActivity;
import cx.ring.client.HomeActivity;
import cx.ring.client.QRCodeActivity;
import cx.ring.contacts.AvatarFactory;
import cx.ring.databinding.FragSmartlistBinding;
import cx.ring.model.CallContact;
import cx.ring.model.Conversation;
import cx.ring.mvp.BaseSupportFragment;
import cx.ring.services.AccountService;
import cx.ring.smartlist.SmartListPresenter;
import cx.ring.smartlist.SmartListView;
import cx.ring.smartlist.SmartListViewModel;
import cx.ring.utils.ActionHelper;
import cx.ring.utils.ClipboardHelper;
import cx.ring.utils.ConversationPath;
import cx.ring.viewholders.SmartListViewHolder;

public class SmartListFragment extends BaseSupportFragment<SmartListPresenter> implements SearchView.OnQueryTextListener,
        SmartListViewHolder.SmartListListeners,
        Conversation.ConversationActionCallback,
        ClipboardHelper.ClipboardHelperCallback,
        SmartListView {

    private static final String TAG = SmartListFragment.class.getSimpleName();
    private static final String STATE_LOADING = TAG + ".STATE_LOADING";

    private static final int SCROLL_DIRECTION_UP = -1;

    @Inject
    AccountService mAccountService;

    private SmartListAdapter mSmartListAdapter;

    private SearchView mSearchView = null;
    private MenuItem mSearchMenuItem = null;

    private FragSmartlistBinding binding;

    @Override
    public void onResume() {
        super.onResume();
        presenter.refresh();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.smartlist_menu, menu);
        mSearchMenuItem = menu.findItem(R.id.menu_contact_search);
        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                binding.newconvFab.show();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                binding.newconvFab.hide();
                return true;
            }
        });

        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.searchbar_hint));
        mSearchView.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.MATCH_PARENT));
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_GO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            EditText editText = mSearchView.findViewById(R.id.search_src_text);
            if (editText != null) {
                editText.setAutofillHints(View.AUTOFILL_HINT_USERNAME);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        Intent intent = activity == null ? null : activity.getIntent();
        if (intent != null)
            handleIntent(intent);
    }

    public void handleIntent(@NonNull Intent intent) {
        if (mSearchView != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_VIEW:
                case Intent.ACTION_CALL:
                    mSearchView.setQuery(intent.getDataString(), true);
                    break;
                case Intent.ACTION_DIAL:
                    mSearchMenuItem.expandActionView();
                    mSearchView.setQuery(intent.getDataString(), false);
                    break;
                case Intent.ACTION_SEARCH:
                    mSearchMenuItem.expandActionView();
                    mSearchView.setQuery(intent.getStringExtra(SearchManager.QUERY), true);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_contact_search:
                mSearchView.setInputType(EditorInfo.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                );
                return false;
            case R.id.menu_scan_qr:
                presenter.clickQRSearch();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        presenter.newContactClicked();
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // if there's another fragment on top of this one, when a rotation is done, this fragment is destroyed and
        // in the process of recreating it, as it is not shown on the top of the screen, the "onCreateView" method is never called, so the mLoader is null
        if (binding != null)
            outState.putBoolean(STATE_LOADING, binding.loadingIndicator.isShown());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        presenter.queryTextChanged(query);
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragSmartlistBinding.inflate(inflater, container, false);
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

        binding.newcontactElement.setVisibility(View.GONE);

        binding.confsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                boolean canScrollUp = recyclerView.canScrollVertically(SCROLL_DIRECTION_UP);
                ExtendedFloatingActionButton btn = binding.newconvFab;
                boolean isExtended = btn.isExtended();
                if (dy > 0 && isExtended) {
                    btn.shrink();
                } else if ((dy < 0 || !canScrollUp) && !isExtended) {
                    btn.extend();
                }

                HomeActivity activity = (HomeActivity) getActivity();
                if (activity != null)
                    activity.setToolbarElevation(canScrollUp);
            }
        });

        DefaultItemAnimator animator = (DefaultItemAnimator) binding.confsList.getItemAnimator();
        if (animator != null) {
            animator.setSupportsChangeAnimations(false);
        }

        binding.newcontactElement.setOnClickListener(v -> presenter.newContactClicked());
        //binding.quickCall.setOnClickListener(v -> presenter.quickCallClicked());
        binding.newconvFab.setOnClickListener(v -> presenter.fabButtonClicked());
    }

    @Override
    public void setLoading(final boolean loading) {
        binding.loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void removeConversation(CallContact callContact) {
        presenter.removeConversation(callContact);
    }

    @Override
    public void clearConversation(CallContact callContact) {
        presenter.clearConversation(callContact);
    }

    @Override
    public void copyContactNumberToClipboard(String contactNumber) {
        ClipboardHelper.copyNumberToClipboard(getActivity(), contactNumber, this);
    }

    @Override
    public void clipBoardDidCopyNumber(String copiedNumber) {
        String snackbarText = getString(R.string.conversation_action_copied_peer_number_clipboard,
                ActionHelper.getShortenedNumber(copiedNumber));
        Snackbar.make(binding.listCoordinator, snackbarText, Snackbar.LENGTH_LONG).show();
    }

    public void onFabButtonClicked() {
        presenter.fabButtonClicked();
    }

    @Override
    public void displayContact(final CallContact contact) {
        TextView display_name = binding.newcontactElement.findViewById(R.id.display_name);
        display_name.setText(contact.getRingUsername());

        ImageView photo = binding.newcontactElement.findViewById(R.id.photo);

        AvatarFactory.loadGlideAvatar(photo, contact);
        binding.newcontactElement.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayNoConversationMessage() {
        String emptyText = getResources().getQuantityString(R.plurals.home_conferences_title, 0, 0);
        binding.emptyTextView.setText(emptyText);
        binding.emptyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayConversationDialog(final SmartListViewModel smartListViewModel) {
        new MaterialAlertDialogBuilder(requireContext())
                .setItems(R.array.conversation_actions, (dialog, which) -> {
                    switch (which) {
                        case ActionHelper.ACTION_COPY:
                            presenter.copyNumber(smartListViewModel);
                            break;
                        case ActionHelper.ACTION_CLEAR:
                            presenter.clearConversation(smartListViewModel);
                            break;
                        case ActionHelper.ACTION_DELETE:
                            presenter.removeConversation(smartListViewModel);
                            break;
                        case ActionHelper.ACTION_BLOCK:
                            presenter.banContact(smartListViewModel);
                            break;
                    }
                })
                .show();
    }

    @Override
    public void displayClearDialog(CallContact callContact) {
        ActionHelper.launchClearAction(getActivity(), callContact, SmartListFragment.this);
    }

    @Override
    public void displayDeleteDialog(CallContact callContact) {
        ActionHelper.launchDeleteAction(getActivity(), callContact, SmartListFragment.this);
    }

    @Override
    public void copyNumber(CallContact callContact) {
        ActionHelper.launchCopyNumberToClipboardFromContact(getActivity(), callContact, this);
    }

    @Override
    public void displayMenuItem() {
        if (mSearchMenuItem != null) {
            mSearchMenuItem.expandActionView();
        }
    }

    @Override
    public void hideSearchRow() {
        binding.newcontactElement.setVisibility(View.GONE);
    }


    @Override
    public void hideList() {
        binding.confsList.setVisibility(View.GONE);
    }

    @Override
    public void hideNoConversationMessage() {
        binding.emptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void updateList(@Nullable final List<SmartListViewModel> smartListViewModels) {
        if (binding == null)
            return;
        if (binding.confsList.getAdapter() == null) {
            mSmartListAdapter = new SmartListAdapter(smartListViewModels, SmartListFragment.this);
            binding.confsList.setAdapter(mSmartListAdapter);
            binding.confsList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(RecyclerView.VERTICAL);
            binding.confsList.setLayoutManager(llm);
        } else {
            mSmartListAdapter.update(smartListViewModels);
        }
        binding.confsList.setVisibility(View.VISIBLE);
    }

    @Override
    public void update(int position) {
        Log.w(TAG, "update " + position + " " + mSmartListAdapter);
        if (mSmartListAdapter != null) {
            mSmartListAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void update(SmartListViewModel model) {
        if (mSmartListAdapter != null)
            mSmartListAdapter.update(model);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HomeActivity.REQUEST_CODE_QR_CONVERSATION && data != null && resultCode == Activity.RESULT_OK) {
            String contactID = data.getStringExtra(ConversationFragment.KEY_CONTACT_RING_ID);
            if (contactID != null) {
                presenter.startConversation(new cx.ring.model.Uri(contactID));
            }
        }
    }

    @Override
    public void goToConversation(String accountId, cx.ring.model.Uri contactId) {
        if (mSearchMenuItem != null) {
            mSearchMenuItem.collapseActionView();
        }
        startActivity(new Intent(Intent.ACTION_VIEW, ConversationPath.toUri(accountId, contactId.toString()), requireContext(), ConversationActivity.class));
    }

    @Override
    public void goToQRActivity() {
        Intent intent = new Intent(QRCodeActivity.ACTION_SCAN)
                .setClass(requireActivity(), QRCodeActivity.class);
        startActivityForResult(intent, HomeActivity.REQUEST_CODE_QR_CONVERSATION);
    }

    @Override
    public void goToContact(CallContact callContact) {
        ActionHelper.displayContact(getActivity(), callContact);
    }

    @Override
    public void scrollToTop() {
        if (binding != null)
            binding.confsList.scrollToPosition(0);
    }

    @Override
    public void onItemClick(SmartListViewModel smartListViewModel) {
        presenter.conversationClicked(smartListViewModel);
    }

    @Override
    public void onItemLongClick(SmartListViewModel smartListViewModel) {
        presenter.conversationLongClicked(smartListViewModel);
    }
}
