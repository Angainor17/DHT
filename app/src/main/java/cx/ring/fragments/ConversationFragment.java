package cx.ring.fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cx.ring.BuildConfig;
import cx.ring.R;
import cx.ring.adapters.ConversationAdapter;
import cx.ring.application.JamiApplication;
import cx.ring.client.ContactDetailsActivity;
import cx.ring.client.ConversationActivity;
import cx.ring.client.HomeActivity;
import cx.ring.contacts.AvatarFactory;
import cx.ring.conversation.ConversationPresenter;
import cx.ring.conversation.ConversationView;
import cx.ring.databinding.FragConversationBinding;
import cx.ring.interfaces.Colorable;
import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.DataTransfer;
import cx.ring.model.Error;
import cx.ring.model.Interaction;
import cx.ring.model.Phone;
import cx.ring.model.Uri;
import cx.ring.mvp.BaseSupportFragment;
import cx.ring.utils.AndroidFileUtils;
import cx.ring.utils.ContentUriHandler;
import cx.ring.utils.ConversationPath;
import cx.ring.views.AvatarDrawable;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;

public class ConversationFragment extends BaseSupportFragment<ConversationPresenter> implements
        ConversationView, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = ConversationFragment.class.getSimpleName();

    public static final String KEY_CONTACT_RING_ID = BuildConfig.APPLICATION_ID + ".CONTACT_RING_ID";
    public static final String KEY_ACCOUNT_ID = BuildConfig.APPLICATION_ID + ".ACCOUNT_ID";
    public static final String KEY_PREFERENCE_PENDING_MESSAGE = "pendingMessage";
    public static final String KEY_PREFERENCE_CONVERSATION_COLOR = "color";
    public static final String EXTRA_SHOW_MAP = "showMap";

    private static final int REQUEST_CODE_FILE_PICKER = 1000;
    private static final int REQUEST_PERMISSION_CAMERA = 1001;
    private static final int REQUEST_CODE_TAKE_PICTURE = 1002;
    private static final int REQUEST_CODE_SAVE_FILE = 1003;
    private static final int REQUEST_CODE_CAPTURE_AUDIO = 1004;
    private static final int REQUEST_CODE_CAPTURE_VIDEO = 1005;

    private ServiceConnection locationServiceConnection = null;

    private FragConversationBinding binding;
//    private MenuItem mAudioCallBtn = null;
//    private MenuItem mVideoCallBtn = null;

    private View currentBottomView = null;
    private ConversationAdapter mAdapter = null;
    private int marginPx;
    private int marginPxTotal;
    private final ValueAnimator animation = new ValueAnimator();

    private SharedPreferences mPreferences;

    private File mCurrentPhoto = null;
    private String mCurrentFileAbsolutePath = null;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private int mSelectedPosition;

    private AvatarDrawable mConversationAvatar;
    private final Map<String, AvatarDrawable> mParticipantAvatars = new HashMap<>();
    private final Map<String, AvatarDrawable> mSmallParticipantAvatars = new HashMap<>();

    public AvatarDrawable getConversationAvatar(String uri) {
        return mParticipantAvatars.get(uri);
    }

    public AvatarDrawable getSmallConversationAvatar(String uri) {
        synchronized (mSmallParticipantAvatars) {
            return mSmallParticipantAvatars.get(uri);
        }
    }

    private static int getIndex(Spinner spinner, Uri myString) {
        for (int i = 0, n = spinner.getCount(); i < n; i++)
            if (((Phone) spinner.getItemAtPosition(i)).getNumber().equals(myString)) {
                return i;
            }
        return 0;
    }

    @Override
    public void refreshView(final List<Interaction> conversation) {
        if (conversation == null) {
            return;
        }
        if (binding != null)
            binding.pbLoading.setVisibility(View.GONE);
        if (mAdapter != null) {
            mAdapter.updateDataset(conversation);
        }
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void scrollToEnd() {
        if (mAdapter.getItemCount() > 0) {
            binding.histList.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    private static void setBottomPadding(@NonNull View view, int padding) {
        view.setPadding(
                view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                padding);
    }

    private void updateListPadding() {
        if (currentBottomView != null && currentBottomView.getHeight() != 0) {
            setBottomPadding(binding.histList, currentBottomView.getHeight() + marginPxTotal);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((JamiApplication) getActivity().getApplication()).getInjectionComponent().inject(this);
        Resources res = getResources();
        marginPx = res.getDimensionPixelSize(R.dimen.conversation_message_input_margin);
        marginPxTotal = marginPx;

        binding = FragConversationBinding.inflate(inflater, container, false);
        binding.setPresenter(this);

        animation.setDuration(150);
        animation.addUpdateListener(valueAnimator -> setBottomPadding(binding.histList, (Integer) valueAnimator.getAnimatedValue()));

        ViewCompat.setOnApplyWindowInsetsListener(binding.histList, (v, insets) -> {
            marginPxTotal = marginPx + insets.getSystemWindowInsetBottom();
            updateListPadding();
            insets.consumeSystemWindowInsets();
            return insets;
        });
        View layout = binding.conversationLayout;

        int paddingTop = layout.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
            v.setPadding(
                    v.getPaddingLeft(),
                    paddingTop + insets.getSystemWindowInsetTop(),
                    v.getPaddingRight(),
                    v.getPaddingBottom());
            insets.consumeSystemWindowInsets();
            return insets;
        });

//        binding.ongoingcallPane.setVisibility(View.GONE);
        binding.msgInputTxt.setMediaListener(contentInfo -> startFileSend(AndroidFileUtils
                .getCacheFile(requireContext(), contentInfo.getContentUri())
                .flatMapCompletable(this::sendFile)
                .doFinally(contentInfo::releasePermission)));
        binding.msgInputTxt.setOnEditorActionListener((v, actionId, event) -> actionSendMsgText(actionId));
        binding.msgInputTxt.setOnFocusChangeListener((view, hasFocus) -> {
        });
        binding.msgInputTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String message = s.toString();
                boolean hasMessage = !TextUtils.isEmpty(message);
                presenter.onComposingChanged(hasMessage);
                if (hasMessage) {
                    binding.msgSend.setVisibility(View.VISIBLE);
                    binding.emojiSend.setVisibility(View.GONE);
                } else {
                    binding.msgSend.setVisibility(View.GONE);
                    binding.emojiSend.setVisibility(View.VISIBLE);
                }
                if (mPreferences != null) {
                    if (hasMessage)
                        mPreferences.edit().putString(KEY_PREFERENCE_PENDING_MESSAGE, message).apply();
                    else
                        mPreferences.edit().remove(KEY_PREFERENCE_PENDING_MESSAGE).apply();
                }
            }
        });

        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPreferences != null) {
            String pendingMessage = mPreferences.getString(KEY_PREFERENCE_PENDING_MESSAGE, null);
            if (!TextUtils.isEmpty(pendingMessage)) {
                binding.msgInputTxt.setText(pendingMessage);
                binding.msgSend.setVisibility(View.VISIBLE);
                binding.emojiSend.setVisibility(View.GONE);
            }
        }

        binding.msgInputTxt.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (oldBottom == 0 && oldTop == 0) {
                updateListPadding();
            } else {
                if (animation.isStarted())
                    animation.cancel();
                animation.setIntValues(binding.histList.getPaddingBottom(), (currentBottomView == null ? 0 : currentBottomView.getHeight()) + marginPxTotal);
                animation.start();
            }
        });

        DefaultItemAnimator animator = (DefaultItemAnimator) binding.histList.getItemAnimator();
        if (animator != null)
            animator.setSupportsChangeAnimations(false);
        binding.histList.setAdapter(mAdapter);
    }

    @Override
    public void setConversationColor(int color) {
        Colorable activity = (Colorable) getActivity();
        if (activity != null)
            activity.setColor(color);
        mAdapter.setPrimaryColor(color);
    }

    @Override
    public void onDestroyView() {
        if (mPreferences != null)
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        animation.removeAllUpdateListeners();
        binding.histList.setAdapter(null);
        mCompositeDisposable.clear();
        if (locationServiceConnection != null) {
            try {
                requireContext().unbindService(locationServiceConnection);
            } catch (Exception e) {
                Log.w(TAG, "Error unbinding service: " + e.getMessage());
            }
        }
        mAdapter = null;
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (mAdapter.onContextItemSelected(item))
            return true;
        return super.onContextItemSelected(item);
    }

    public void updateAdapterItem() {
        if (mSelectedPosition != -1) {
            mAdapter.notifyItemChanged(mSelectedPosition);
            mSelectedPosition = -1;
        }
    }

    public void sendMessageText() {
        String message = binding.msgInputTxt.getText().toString();
        clearMsgEdit();
        presenter.sendTextMessage(message);
    }

    public void sendEmoji() {
        presenter.sendTextMessage(binding.emojiSend.getText().toString());
    }

    @SuppressLint("RestrictedApi")
    public void selectFile() {
        presenter.selectFile();
    }

    /**
     * Used to update with the past adapter position when a long click was registered
     */
    public void updatePosition(int position) {
        mSelectedPosition = position;
    }


    public void sendAudioMessage() {
        if (!presenter.getDeviceRuntimeService().hasAudioPermission()) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_CAPTURE_AUDIO);
        } else {
            Context ctx = requireContext();
            Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);

            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                try {
                    mCurrentPhoto = AndroidFileUtils.createAudioFile(ctx);
                } catch (IOException ex) {
                    Log.e(TAG, "takePicture: error creating temporary file", ex);
                    return;
                }
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_AUDIO);
            } else {
                Toast.makeText(getActivity(), "Can't find audio recorder app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendVideoMessage() {
        if (!presenter.getDeviceRuntimeService().hasVideoPermission()) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAPTURE_VIDEO);
        } else {
            Context context = requireContext();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                try {
                    mCurrentPhoto = AndroidFileUtils.createVideoFile(context);
                } catch (IOException ex) {
                    Log.e(TAG, "takePicture: error creating temporary file", ex);
                    return;
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ContentUriHandler.getUriForFile(context, ContentUriHandler.AUTHORITY_FILES, mCurrentPhoto));
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO);
            } else {
                Toast.makeText(getActivity(), "Can't find video recorder app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void takePicture() {
        if (!presenter.getDeviceRuntimeService().hasVideoPermission()) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_TAKE_PICTURE);
        } else {
            Context c = getContext();
            if (c == null)
                return;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(c.getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    File photoFile = AndroidFileUtils.createImageFile(c);
                    Log.i(TAG, "takePicture: trying to save to " + photoFile);
                    android.net.Uri photoURI = ContentUriHandler.getUriForFile(c, ContentUriHandler.AUTHORITY_FILES, photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            .putExtra("android.intent.extras.CAMERA_FACING", 1)
                            .putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                            .putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                    mCurrentPhoto = photoFile;
                    startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
                } catch (Exception e) {
                    Toast.makeText(c, "Error taking picture: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void askWriteExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, JamiApplication.PERMISSIONS_REQUEST);
    }

    @Override
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_FILE_PICKER);
    }

    private Completable sendFile(File file) {
        return Completable.fromAction(() -> presenter.sendFile(file));
    }

    private void startFileSend(Completable op) {
        setLoading(true);
        op.observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> setLoading(false))
                .subscribe(() -> {
                }, e -> {
                    Log.e(TAG, "startFileSend: not able to create cache file", e);
                    displayErrorToast(Error.INVALID_FILE);
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.w(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + resultData);
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == RESULT_OK) {
            if (resultData == null) {
                return;
            }
            android.net.Uri uri = resultData.getData();
            if (uri == null) {
                return;
            }
            startFileSend(AndroidFileUtils.getCacheFile(requireContext(), uri)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapCompletable(this::sendFile));
        } else if (requestCode == REQUEST_CODE_TAKE_PICTURE
                || requestCode == REQUEST_CODE_CAPTURE_AUDIO
                || requestCode == REQUEST_CODE_CAPTURE_VIDEO) {
            if (resultCode != RESULT_OK) {
                mCurrentPhoto = null;
                return;
            }
            Log.w(TAG, "onActivityResult: mCurrentPhoto " + mCurrentPhoto.getAbsolutePath() + " " + mCurrentPhoto.exists() + " " + mCurrentPhoto.length());
            Single<File> file = null;
            if (mCurrentPhoto == null || !mCurrentPhoto.exists() || mCurrentPhoto.length() == 0) {
                android.net.Uri createdUri = resultData.getData();
                if (createdUri != null) {
                    file = AndroidFileUtils.getCacheFile(requireContext(), createdUri);
                }
            } else {
                file = Single.just(mCurrentPhoto);
            }
            mCurrentPhoto = null;
            if (file == null) {
                Toast.makeText(getActivity(), "Can't find picture", Toast.LENGTH_SHORT).show();
                return;
            }
            startFileSend(file.flatMapCompletable(this::sendFile));
        }
        // File download trough SAF
        else if (requestCode == ConversationFragment.REQUEST_CODE_SAVE_FILE
                && resultCode == RESULT_OK) {
            if (resultData != null && resultData.getData() != null) {
                //Get the Uri of the file that was created by the app that received our intent
                android.net.Uri createdUri = resultData.getData();

                //Try to copy the data of the current file into the newly created one
                File input = new File(mCurrentFileAbsolutePath);
                if (requireContext().getContentResolver() != null)
                    mCompositeDisposable.add(AndroidFileUtils.copyFileToUri(requireContext().getContentResolver(), input, createdUri).
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribe(() -> Toast.makeText(getContext(), R.string.file_saved_successfully, Toast.LENGTH_SHORT).show(),
                                    error -> Toast.makeText(getContext(), R.string.generic_error, Toast.LENGTH_SHORT).show()));

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0, n = permissions.length; i < n; i++) {
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            switch (permissions[i]) {
                case Manifest.permission.CAMERA:
                    presenter.cameraPermissionChanged(granted);
                    if (granted) {
                        if (requestCode == REQUEST_CODE_CAPTURE_VIDEO) {
                            sendVideoMessage();
                        } else if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
                            takePicture();
                        }
                    }
                    return;
                case Manifest.permission.RECORD_AUDIO:
                    if (granted) {
                        if (requestCode == REQUEST_CODE_CAPTURE_AUDIO) {
                            sendAudioMessage();
                        }
                    }
                    return;
                default:
                    break;
            }
        }
    }

    @Override
    public void addElement(Interaction element) {
        mAdapter.add(element);
        scrollToEnd();
    }

    @Override
    public void updateElement(Interaction element) {
        mAdapter.update(element);
    }

    @Override
    public void removeElement(Interaction element) {
        mAdapter.remove(element);
    }

    @Override
    public void setComposingStatus(Account.ComposingStatus composingStatus) {
        mAdapter.setComposingStatus(composingStatus);
        if (composingStatus == Account.ComposingStatus.Active)
            scrollToEnd();
    }

    @Override
    public void setLastDisplayed(Interaction interaction) {
        mAdapter.setLastDisplayed(interaction);
    }

    @Override
    public void shareFile(File path) {
        Context c = getContext();
        if (c == null)
            return;
        android.net.Uri fileUri = null;
        try {
            fileUri = ContentUriHandler.getUriForFile(c, ContentUriHandler.AUTHORITY_FILES, path);
        } catch (IllegalArgumentException e) {
            Log.e("File Selector", "The selected file can't be shared: " + path.getName());
        }
        if (fileUri != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String type = c.getContentResolver().getType(fileUri);
            sendIntent.setDataAndType(fileUri, type);
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(sendIntent, null));
        }
    }

    @Override
    public void openFile(File path) {
        Context c = getContext();
        if (c == null)
            return;
        android.net.Uri fileUri = null;
        try {
            fileUri = ContentUriHandler.getUriForFile(c, ContentUriHandler.AUTHORITY_FILES, path);
        } catch (IllegalArgumentException e) {
            Log.e("File Selector", "The selected file can't be shared: " + path.getName());
        }
        if (fileUri != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_VIEW);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String type = c.getContentResolver().getType(fileUri);
            sendIntent.setDataAndType(fileUri, type);
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            //startActivity(Intent.createChooser(sendIntent, null));
            try {
                startActivity(sendIntent);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(getView(), R.string.conversation_open_file_error, Snackbar.LENGTH_LONG).show();
                Log.e("File Loader", "File of unknown type, could not open: " + path.getName());
            }
        }
    }

    boolean actionSendMsgText(int actionId) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEND:
                sendMessageText();
                return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!isVisible()) {
            return;
        }
        inflater.inflate(R.menu.conversation_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(getActivity(), HomeActivity.class));
                return true;
//            case R.id.conv_action_audiocall:
//                presenter.goToCall(true);
//                return true;
//            case R.id.conv_action_videocall:
//                presenter.goToCall(false);
//                return true;
            case R.id.conv_contact_details:
                presenter.openContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void initPresenter(ConversationPresenter presenter) {
        ConversationPath path = ConversationPath.fromBundle(getArguments());
        if (path == null)
            return;
        Uri contactUri = new Uri(path.getContactId());
        mAdapter = new ConversationAdapter(this, presenter);
        presenter.init(contactUri, path.getAccountId());
        try {
            mPreferences = requireActivity().getSharedPreferences(path.getAccountId() + "_" + contactUri.getRawRingId(), Context.MODE_PRIVATE);
            mPreferences.registerOnSharedPreferenceChangeListener(this);
            presenter.setConversationColor(mPreferences.getInt(KEY_PREFERENCE_CONVERSATION_COLOR, getResources().getColor(R.color.color_primary_light)));
        } catch (Exception e) {
            Log.e(TAG, "Can't load conversation preferences");
        }

        if (locationServiceConnection == null) {
            locationServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.w(TAG, "onServiceConnected");

                    try {
                        requireContext().unbindService(locationServiceConnection);
                    } catch (Exception e) {
                        Log.w(TAG, "Error unbinding service", e);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.w(TAG, "onServiceDisconnected");
                    locationServiceConnection = null;
                }
            };
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case KEY_PREFERENCE_CONVERSATION_COLOR:
                presenter.setConversationColor(prefs.getInt(KEY_PREFERENCE_CONVERSATION_COLOR, getResources().getColor(R.color.color_primary_light)));
                break;
        }
    }

    @Override
    public void displayContact(final CallContact contact) {
        mCompositeDisposable.clear();
        mCompositeDisposable.add(AvatarFactory.getAvatar(requireContext(), contact)
                .doOnSuccess(d -> {
                    mConversationAvatar = (AvatarDrawable) d;
                    mParticipantAvatars.put(contact.getPrimaryNumber(),
                            new AvatarDrawable((AvatarDrawable) d));
                })
                .flatMapObservable(d -> contact.getUpdatesSubject())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(c -> {
                    mConversationAvatar.update(c);
                    String uri = contact.getPrimaryNumber();
                    AvatarDrawable ad = mParticipantAvatars.get(uri);
                    if (ad != null)
                        ad.update(c);
                    setupActionbar(contact);
                    mAdapter.setPhoto();
                }));
        mCompositeDisposable.add(AvatarFactory.getAvatar(requireContext(), contact, false)
                .doOnSuccess(d -> mSmallParticipantAvatars.put(contact.getPrimaryNumber(), new AvatarDrawable((AvatarDrawable) d)))
                .flatMapObservable(d -> contact.getUpdatesSubject())
                .subscribe(c -> {
                    synchronized (mSmallParticipantAvatars) {
                        String uri = contact.getPrimaryNumber();
                        AvatarDrawable ad = mSmallParticipantAvatars.get(uri);
                        if (ad != null)
                            ad.update(c);
                    }
                }));
    }
//
//    @Override
//    public void displayOnGoingCallPane(final boolean display) {
//        binding.ongoingcallPane.setVisibility(display ? View.VISIBLE : View.GONE);
//    }
//
//    @Override
//    public void displayNumberSpinner(final Conversation conversation, final Uri number) {
//        binding.numberSelector.setVisibility(View.VISIBLE);
//        binding.numberSelector.setAdapter(new NumberAdapter(getActivity(),
//                conversation.getContact(), false));
//        binding.numberSelector.setSelection(getIndex(binding.numberSelector, number));
//    }
//
//    @Override
//    public void hideNumberSpinner() {
//        binding.numberSelector.setVisibility(View.GONE);
//    }

    @Override
    public void clearMsgEdit() {
        binding.msgInputTxt.setText("");
    }

    @Override
    public void goToHome() {
        if (getActivity() instanceof ConversationActivity) {
            getActivity().finish();
        }
    }

//    @Override
//    public void goToAddContact(CallContact callContact) {
//        startActivityForResult(ActionHelper.getAddNumberIntentForContact(callContact), REQ_ADD_CONTACT);
//    }

    @Override
    public void goToContactActivity(String accountId, String contactRingId) {
        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.withAppendedPath(android.net.Uri.withAppendedPath(ContentUriHandler.CONTACT_CONTENT_URI, accountId), contactRingId))
                .setClass(requireActivity().getApplicationContext(), ContactDetailsActivity.class));
    }

    private void setupActionbar(CallContact contact) {
        if (!isVisible()) {
            return;
        }

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        Context context = actionBar.getThemedContext();
        String displayName = contact.getDisplayName();
        String identity = contact.getRingUsername();

        Activity activity = getActivity();
        if (activity instanceof HomeActivity) {
            Toolbar toolbar = getActivity().findViewById(R.id.main_toolbar);
            TextView title = toolbar.findViewById(R.id.contact_title);
            TextView subtitle = toolbar.findViewById(R.id.contact_subtitle);
            ImageView logo = toolbar.findViewById(R.id.contact_image);

            if (!((HomeActivity) activity).isConversationSelected()) {
                title.setText("");
                subtitle.setText("");
                logo.setImageDrawable(null);
                return;
            }

            logo.setVisibility(View.VISIBLE);
            title.setText(displayName);
            title.setTextSize(15);
            title.setTypeface(null, Typeface.NORMAL);

            if (identity != null && !identity.equals(displayName)) {
                subtitle.setText(identity);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) title.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_TOP, R.id.contact_image);
                title.setLayoutParams(params);
            } else {
                subtitle.setText("");

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) title.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_TOP);
                params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                title.setLayoutParams(params);
            }

            logo.setImageDrawable(mConversationAvatar);
        } else {
            if (identity != null && !identity.equals(displayName)) {
                actionBar.setSubtitle(identity);
            }
            actionBar.setTitle(displayName);
            int targetSize = (int) (AvatarFactory.SIZE_AB * context.getResources().getDisplayMetrics().density);
            mConversationAvatar.setInSize(targetSize);
            actionBar.setLogo(null);
            actionBar.setLogo(mConversationAvatar);
        }
    }

    public void refuseContactRequest() {
        presenter.onRefuseIncomingContactRequest();
    }

    public void acceptContactRequest() {
        presenter.onAcceptIncomingContactRequest();
    }

    public void addContact() {
        presenter.onAddContact();
    }

    @Override
    public void switchToUnknownView(String contactDisplayName) {
        binding.cvMessageInput.setVisibility(View.GONE);
        binding.unknownContactPrompt.setVisibility(View.VISIBLE);
        binding.trustRequestPrompt.setVisibility(View.GONE);
        binding.tvTrustRequestMessage.setText(String.format(getString(R.string.message_contact_not_trusted), contactDisplayName));
        binding.trustRequestMessageLayout.setVisibility(View.VISIBLE);
        currentBottomView = binding.unknownContactPrompt;
        requireActivity().invalidateOptionsMenu();
        updateListPadding();
    }

    @Override
    public void switchToIncomingTrustRequestView(String contactDisplayName) {
        binding.cvMessageInput.setVisibility(View.GONE);
        binding.unknownContactPrompt.setVisibility(View.GONE);
        binding.trustRequestPrompt.setVisibility(View.VISIBLE);
        binding.tvTrustRequestMessage.setText(String.format(getString(R.string.message_contact_not_trusted_yet), contactDisplayName));
        binding.trustRequestMessageLayout.setVisibility(View.VISIBLE);
        currentBottomView = binding.trustRequestPrompt;
        requireActivity().invalidateOptionsMenu();
        updateListPadding();
    }

    @Override
    public void switchToConversationView() {
        binding.cvMessageInput.setVisibility(View.VISIBLE);
        binding.unknownContactPrompt.setVisibility(View.GONE);
        binding.trustRequestPrompt.setVisibility(View.GONE);
        binding.trustRequestMessageLayout.setVisibility(View.GONE);
        currentBottomView = binding.cvMessageInput;
        requireActivity().invalidateOptionsMenu();
        updateListPadding();
    }

    private void setLoading(boolean isLoading) {
        if (binding == null)
            return;
        if (isLoading) {
            binding.btnTakePicture.setVisibility(View.GONE);
            binding.pbDataTransfer.setVisibility(View.VISIBLE);
        } else {
            binding.btnTakePicture.setVisibility(View.VISIBLE);
            binding.pbDataTransfer.setVisibility(View.GONE);
        }
    }

    public void handleShareIntent(Intent intent) {
        Log.w(TAG, "handleShareIntent " + intent);

        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            String type = intent.getType();
            if (type == null) {
                Log.w(TAG, "Can't share with no type");
                return;
            }
            if (type.startsWith("text/plain")) {
                binding.msgInputTxt.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            } else {
                android.net.Uri uri = intent.getData();
                ClipData clip = intent.getClipData();
                if (uri == null && clip != null && clip.getItemCount() > 0)
                    uri = clip.getItemAt(0).getUri();
                if (uri == null)
                    return;
                startFileSend(AndroidFileUtils.getCacheFile(requireContext(), uri).flatMapCompletable(this::sendFile));
            }
        }
    }

    /**
     * Creates an intent using Android Storage Access Framework
     * This intent is then received by applications that can handle it like
     * Downloads or Google drive
     *
     * @param file                    DataTransfer of the file that is going to be stored
     * @param currentFileAbsolutePath absolute path of the file we want to save
     */
    public void startSaveFile(DataTransfer file, String currentFileAbsolutePath) {
        //Get the current file absolute path and store it
        mCurrentFileAbsolutePath = currentFileAbsolutePath;

        //Use Android Storage File Access to download the file
        Intent downloadFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        downloadFileIntent.setType(AndroidFileUtils.getMimeTypeFromExtension(file.getExtension()));
        downloadFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        downloadFileIntent.putExtra(Intent.EXTRA_TITLE, file.getDisplayName());

        startActivityForResult(downloadFileIntent, ConversationFragment.REQUEST_CODE_SAVE_FILE);
    }

    @Override
    public void displayNetworkErrorPanel() {
        if (binding != null) {
            binding.errorMsgPane.setVisibility(View.VISIBLE);
            binding.errorMsgPane.setOnClickListener(null);
            binding.errorMsgPane.setText(R.string.error_no_network);
        }
    }

    @Override
    public void hideErrorPanel() {
        if (binding != null) {
            binding.errorMsgPane.setVisibility(View.GONE);
        }
    }

}
