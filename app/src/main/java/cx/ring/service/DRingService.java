package cx.ring.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;
import androidx.legacy.content.WakefulBroadcastReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import cx.ring.BuildConfig;
import cx.ring.application.JamiApplication;
import cx.ring.client.ConversationActivity;
import cx.ring.facades.ConversationFacade;
import cx.ring.model.Codec;
import cx.ring.model.Settings;
import cx.ring.model.Uri;
import cx.ring.services.AccountService;
import cx.ring.services.CallService;
import cx.ring.services.ContactService;
import cx.ring.services.DaemonService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HardwareService;
import cx.ring.services.HistoryService;
import cx.ring.services.NotificationService;
import cx.ring.services.PreferencesService;
import cx.ring.utils.ConversationPath;
import io.reactivex.disposables.CompositeDisposable;

public class DRingService extends Service {
    private static final String TAG = DRingService.class.getSimpleName();

    public static final String ACTION_TRUST_REQUEST_ACCEPT = BuildConfig.APPLICATION_ID + ".action.TRUST_REQUEST_ACCEPT";
    public static final String ACTION_TRUST_REQUEST_REFUSE = BuildConfig.APPLICATION_ID + ".action.TRUST_REQUEST_REFUSE";
    public static final String ACTION_TRUST_REQUEST_BLOCK = BuildConfig.APPLICATION_ID + ".action.TRUST_REQUEST_BLOCK";

    static public final String ACTION_CALL_ACCEPT = BuildConfig.APPLICATION_ID + ".action.CALL_ACCEPT";
    static public final String ACTION_CALL_REFUSE = BuildConfig.APPLICATION_ID + ".action.CALL_REFUSE";
    static public final String ACTION_CALL_END = BuildConfig.APPLICATION_ID + ".action.CALL_END";
    static public final String ACTION_CALL_VIEW = BuildConfig.APPLICATION_ID + ".action.CALL_VIEW";

    static public final String ACTION_CONV_READ = BuildConfig.APPLICATION_ID + ".action.CONV_READ";
    static public final String ACTION_CONV_DISMISS = BuildConfig.APPLICATION_ID + ".action.CONV_DISMISS";
    static public final String ACTION_CONV_ACCEPT = BuildConfig.APPLICATION_ID + ".action.CONV_ACCEPT";
    static public final String ACTION_CONV_REPLY_INLINE = BuildConfig.APPLICATION_ID + ".action.CONV_REPLY";

    static public final String ACTION_FILE_ACCEPT = BuildConfig.APPLICATION_ID + ".action.FILE_ACCEPT";
    static public final String ACTION_FILE_CANCEL = BuildConfig.APPLICATION_ID + ".action.FILE_CANCEL";
    static public final String KEY_TRANSFER_ID = "transferId";
    static public final String KEY_TEXT_REPLY = "textReply";

    private static final int NOTIFICATION_ID = 1;

    private final ContactsContentObserver contactContentObserver = new ContactsContentObserver();
    @Inject
    @Singleton
    protected DaemonService mDaemonService;
    @Inject
    @Singleton
    protected CallService mCallService;
    @Inject
    @Singleton
    protected AccountService mAccountService;
    @Inject
    @Singleton
    protected HardwareService mHardwareService;
    @Inject
    @Singleton
    protected HistoryService mHistoryService;
    @Inject
    @Singleton
    protected DeviceRuntimeService mDeviceRuntimeService;
    @Inject
    @Singleton
    protected NotificationService mNotificationService;
    @Inject
    @Singleton
    protected ContactService mContactService;
    @Inject
    @Singleton
    protected PreferencesService mPreferencesService;
    @Inject
    @Singleton
    protected ConversationFacade mConversationFacade;

    private final Handler mHandler = new Handler();
    private final CompositeDisposable mDisposableBag = new CompositeDisposable();
    private final Runnable mConnectivityChecker = this::updateConnectivityState;
    public static boolean isRunning = false;

    protected final IDRingService.Stub mBinder = new IDRingService.Stub() {

        public void sendProfile(final String callId, final String accountId) {
            mAccountService.sendProfile(callId, accountId);
        }

        @Override
        public boolean isStarted() throws RemoteException {
            return mDaemonService.isStarted();
        }

        @Override
        public void setAudioPlugin(final String audioPlugin) {
            mCallService.setAudioPlugin(audioPlugin);
        }

        @Override
        public String getCurrentAudioOutputPlugin() {
            return mCallService.getCurrentAudioOutputPlugin();
        }

        @Override
        public List<String> getAccountList() {
            return mAccountService.getAccountList();
        }

        @Override
        public void setAccountOrder(final String order) {
            String[] accountIds = order.split(File.separator);
            mAccountService.setAccountOrder(Arrays.asList(accountIds));
        }

        @Override
        public Map<String, String> getAccountDetails(final String accountID) {
            return mAccountService.getAccountDetails(accountID);
        }

        @SuppressWarnings("unchecked")
        // Hashmap runtime cast
        @Override
        public void setAccountDetails(final String accountId, final Map map) {
            mAccountService.setAccountDetails(accountId, map);
        }

        @Override
        public void setAccountActive(final String accountId, final boolean active) {
            mAccountService.setAccountActive(accountId, active);
        }

        @Override
        public void setAccountsActive(final boolean active) {
            mAccountService.setAccountsActive(active);
        }

        @Override
        public Map<String, String> getVolatileAccountDetails(final String accountId) {
            return mAccountService.getVolatileAccountDetails(accountId);
        }

        @Override
        public Map<String, String> getAccountTemplate(final String accountType) throws RemoteException {
            return mAccountService.getAccountTemplate(accountType).blockingGet();
        }

        @SuppressWarnings("unchecked")
        // Hashmap runtime cast
        @Override
        public String addAccount(final Map map) {
            return mAccountService.addAccount((Map<String, String>) map).blockingFirst().getAccountID();
        }

        @Override
        public void removeAccount(final String accountId) {
            mAccountService.removeAccount(accountId);
        }

        @Override
        public void exportOnRing(final String accountId, final String password) {
            mAccountService.exportOnRing(accountId, password);
        }

        public Map<String, String> getKnownRingDevices(final String accountId) {
            return mAccountService.getKnownRingDevices(accountId);
        }

        /*************************
         * Transfer related API
         *************************/

        @Override
        public void transfer(final String callID, final String to) throws RemoteException {
            mCallService.transfer(callID, to);
        }

        @Override
        public void attendedTransfer(final String transferID, final String targetID) throws RemoteException {
            mCallService.attendedTransfer(transferID, targetID);
        }

        /*************************
         * Conference related API
         *************************/

        @Override
        public void removeConference(final String confID) throws RemoteException {
            mCallService.removeConference(confID);
        }

        @Override
        public void joinParticipant(final String selCallID, final String dragCallID) throws RemoteException {
            mCallService.joinParticipant(selCallID, dragCallID);
        }

        @Override
        public void addParticipant(final String callID, final String confID) throws RemoteException {
            mCallService.addParticipant(callID, confID);
        }

        @Override
        public void addMainParticipant(final String confID) throws RemoteException {
            mCallService.addMainParticipant(confID);
        }

        @Override
        public void detachParticipant(final String callID) throws RemoteException {
            mCallService.detachParticipant(callID);
        }

        @Override
        public void joinConference(final String selConfID, final String dragConfID) throws RemoteException {
            mCallService.joinConference(selConfID, dragConfID);
        }

        @Override
        public void hangUpConference(final String confID) throws RemoteException {
            mCallService.hangUpConference(confID);
        }

        @Override
        public void holdConference(final String confID) throws RemoteException {
            mCallService.holdConference(confID);
        }

        @Override
        public void unholdConference(final String confID) throws RemoteException {
            mCallService.unholdConference(confID);
        }

        @Override
        public boolean isConferenceParticipant(final String callID) throws RemoteException {
            return mCallService.isConferenceParticipant(callID);
        }

        @Override
        public Map<String, ArrayList<String>> getConferenceList() throws RemoteException {
            return mCallService.getConferenceList();
        }

        @Override
        public List<String> getParticipantList(final String confID) throws RemoteException {
            return mCallService.getParticipantList(confID);
        }

        @Override
        public String getConferenceId(String callID) throws RemoteException {
            return mCallService.getConferenceId(callID);
        }

        @Override
        public String getConferenceDetails(final String callID) throws RemoteException {
            return mCallService.getConferenceState(callID);
        }

        @Override
        public void sendTextMessage(final String callID, final String msg) throws RemoteException {
            mCallService.sendTextMessage(callID, msg);
        }

        @Override
        public long sendAccountTextMessage(final String accountID, final String to, final String msg) {
            return mCallService.sendAccountTextMessage(accountID, to, msg).blockingGet();
        }

        @Override
        public List<Codec> getCodecList(final String accountID) throws RemoteException {
            return mAccountService.getCodecList(accountID).blockingGet();
        }

        @Override
        public Map<String, String> validateCertificatePath(final String accountID, final String certificatePath, final String privateKeyPath, final String privateKeyPass) throws RemoteException {
            return mAccountService.validateCertificatePath(accountID, certificatePath, privateKeyPath, privateKeyPass);
        }

        @Override
        public Map<String, String> validateCertificate(final String accountID, final String certificate) throws RemoteException {
            return mAccountService.validateCertificate(accountID, certificate);
        }

        @Override
        public Map<String, String> getCertificateDetailsPath(final String certificatePath) throws RemoteException {
            return mAccountService.getCertificateDetailsPath(certificatePath);
        }

        @Override
        public Map<String, String> getCertificateDetails(final String certificateRaw) throws RemoteException {
            return mAccountService.getCertificateDetails(certificateRaw);
        }

        @Override
        public void setActiveCodecList(final List codecs, final String accountID) throws RemoteException {
            mAccountService.setActiveCodecList(accountID, codecs);
        }

        @Override
        public void playDtmf(final String key) throws RemoteException {

        }

        @Override
        public Map<String, String> getConference(final String id) throws RemoteException {
            return mCallService.getConferenceDetails(id);
        }

        @Override
        public void setMuted(final boolean mute) throws RemoteException {
            mCallService.setMuted(mute);
        }

        @Override
        public boolean isCaptureMuted() throws RemoteException {
            return mCallService.isCaptureMuted();
        }

        @Override
        public List<String> getTlsSupportedMethods() {
            return mAccountService.getTlsSupportedMethods();
        }

        @Override
        public List getCredentials(final String accountID) throws RemoteException {
            return mAccountService.getCredentials(accountID);
        }

        @Override
        public void setCredentials(final String accountID, final List creds) throws RemoteException {
            mAccountService.setCredentials(accountID, creds);
        }

        @Override
        public void registerAllAccounts() throws RemoteException {
            mAccountService.registerAllAccounts();
        }

        @Override
        public int backupAccounts(final List accountIDs, final String toDir, final String password) {
            return mAccountService.backupAccounts(accountIDs, toDir, password);
        }

        @Override
        public int restoreAccounts(final String archivePath, final String password) {
            return mAccountService.restoreAccounts(archivePath, password);
        }

        @Override
        public void connectivityChanged() {
            mHardwareService.connectivityChanged(mPreferencesService.hasNetworkConnected());
        }

        @Override
        public void lookupName(final String account, final String nameserver, final String name) {
            mAccountService.lookupName(account, nameserver, name);
        }

        @Override
        public void lookupAddress(final String account, final String nameserver, final String address) {
            mAccountService.lookupAddress(account, nameserver, address);
        }

        @Override
        public void registerName(final String account, final String password, final String name) {
            mAccountService.registerName(account, password, name);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Log.w(TAG, "onReceive: received a null action on broadcast receiver");
                return;
            }
            Log.d(TAG, "receiver.onReceive: " + action);
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION: {
                    updateConnectivityState();
                    break;
                }
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                    mHardwareService.initVideo();
                    break;
                }
                case PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED: {
                    mConnectivityChecker.run();
                    mHandler.postDelayed(mConnectivityChecker, 100);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreated");
        super.onCreate();

        // dependency injection
        JamiApplication.getInstance().getInjectionComponent().inject(this);
        isRunning = true;

        if (mDeviceRuntimeService.hasContactPermission()) {
            getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactContentObserver);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentFilter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
        }
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver, intentFilter);
        updateConnectivityState();

        mDisposableBag.add(mPreferencesService.getSettingsSubject().subscribe(s -> {
            showSystemNotification(s);
        }));

        JamiApplication.getInstance().bindDaemon();
        JamiApplication.getInstance().bootstrapDaemon();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        unregisterReceiver(receiver);
        getContentResolver().unregisterContentObserver(contactContentObserver);

        mDisposableBag.clear();
        isRunning = false;
    }

    private void showSystemNotification(Settings settings) {
        if (settings.isAllowPersistentNotification()) {
            startForeground(NOTIFICATION_ID, (Notification) mNotificationService.getServiceNotification());
        } else {
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.i(TAG, "onStartCommand " + (intent == null ? "null" : intent.getAction()) + " " + flags + " " + startId);
        if (intent != null) {
            parseIntent(intent);
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
        return START_STICKY; /* started and stopped explicitly */
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBound");
        return mBinder;
    }

    /* ************************************
     *
     * Implement public interface for the service
     *
     * *********************************
     */

    private void updateConnectivityState() {
        if (mDaemonService.isStarted()) {
            boolean isConnected = mPreferencesService.hasNetworkConnected();
            mAccountService.setAccountsActive(isConnected);
            // Execute connectivityChanged to reload UPnP
            // and reconnect active accounts if necessary.
            mHardwareService.connectivityChanged(isConnected);
        }
    }

    private void parseIntent(@NonNull Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        switch (action) {
            case ACTION_TRUST_REQUEST_ACCEPT:
            case ACTION_TRUST_REQUEST_REFUSE:
            case ACTION_TRUST_REQUEST_BLOCK:
                if (extras != null) {
                    handleTrustRequestAction(action, extras);
                }
                break;
            case ACTION_CONV_READ:
            case ACTION_CONV_ACCEPT:
            case ACTION_CONV_DISMISS:
            case ACTION_CONV_REPLY_INLINE:
                handleConvAction(intent, action, extras);
                break;
            case ACTION_FILE_ACCEPT:
            case ACTION_FILE_CANCEL:
                if (extras != null) {
                    handleFileAction(action, extras);
                }
                break;
            default:
                break;
        }
    }

    private void handleFileAction(String action, Bundle extras) {
        Long id = extras.getLong(KEY_TRANSFER_ID);
        if (action.equals(ACTION_FILE_ACCEPT)) {
            mAccountService.acceptFileTransfer(id);
        } else if (action.equals(ACTION_FILE_CANCEL)) {
            mConversationFacade.cancelFileTransfer(id);
        }
    }

    private void handleTrustRequestAction(String action, Bundle extras) {
        String account = extras.getString(NotificationService.TRUST_REQUEST_NOTIFICATION_ACCOUNT_ID);
        Uri from = new Uri(extras.getString(NotificationService.TRUST_REQUEST_NOTIFICATION_FROM));
        if (account != null) {
            mNotificationService.cancelTrustRequestNotification(account);
            switch (action) {
                case ACTION_TRUST_REQUEST_ACCEPT:
                    mConversationFacade.acceptRequest(account, from);
                    break;
                case ACTION_TRUST_REQUEST_REFUSE:
                    mConversationFacade.discardRequest(account, from);
                    break;
                case ACTION_TRUST_REQUEST_BLOCK:
                    mConversationFacade.discardRequest(account, from);
                    mAccountService.removeContact(account, from.getRawRingId(), true);
                    break;
            }
        }
    }


    private void handleConvAction(Intent intent, String action, Bundle extras) {
        ConversationPath path = ConversationPath.fromIntent(intent);

        if (path == null || path.getContactId().isEmpty()) {
            return;
        }

        switch (action) {
            case ACTION_CONV_READ:
                mConversationFacade.readMessages(path.getAccountId(), new Uri(path.getContactId()));
                break;
            case ACTION_CONV_DISMISS:
                break;
            case ACTION_CONV_REPLY_INLINE: {
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    CharSequence reply = remoteInput.getCharSequence(KEY_TEXT_REPLY);
                    if (!TextUtils.isEmpty(reply)) {
                        Uri uri = new Uri(path.getContactId());
                        String message = reply.toString();
                        mConversationFacade.startConversation(path.getAccountId(), uri)
                                .flatMap(c -> mConversationFacade.sendTextMessage(path.getAccountId(), c, uri, message)
                                        .doOnSuccess(msg -> mNotificationService.showTextNotification(path.getAccountId(), c)))
                                .subscribe();
                    }
                }
                break;
            }
            case ACTION_CONV_ACCEPT:
                startActivity(new Intent(Intent.ACTION_VIEW, path.toUri(), getApplicationContext(), ConversationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            default:
                break;
        }
    }

    public void refreshContacts() {
        if (mAccountService.getCurrentAccount() == null) {
            return;
        }
        mContactService.loadContacts(mAccountService.hasRingAccount(), mAccountService.hasSipAccount(), mAccountService.getCurrentAccount());
    }

    private static class ContactsContentObserver extends ContentObserver {

        ContactsContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, android.net.Uri uri) {
            super.onChange(selfChange, uri);
            //mContactService.loadContacts(mAccountService.hasRingAccount(), mAccountService.hasSipAccount(), mAccountService.getCurrentAccount());
        }
    }
}
