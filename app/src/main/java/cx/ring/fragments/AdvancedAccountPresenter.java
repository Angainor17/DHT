package cx.ring.fragments;

import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.inject.Inject;

import cx.ring.facades.ConversationFacade;
import cx.ring.model.Account;
import cx.ring.model.ConfigKey;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;

public class AdvancedAccountPresenter extends RootPresenter<AdvancedAccountView> {

    public static final String TAG = AdvancedAccountPresenter.class.getSimpleName();

    protected ConversationFacade mConversationFacade;
    protected AccountService mAccountService;

    private Account mAccount;

    @Inject
    public AdvancedAccountPresenter(ConversationFacade conversationFacade, AccountService accountService) {
        this.mConversationFacade = conversationFacade;
        this.mAccountService = accountService;
    }

    public void init(String accountId) {
        mAccount = mAccountService.getAccount(accountId);
        if (mAccount != null) {
            getView().initView(mAccount.getConfig(), getNetworkInterfaces());
        }
    }

    public void twoStatePreferenceChanged(ConfigKey configKey, Object newValue) {
        mAccount.setDetail(configKey, newValue.toString());
        updateAccount();
    }

    public void passwordPreferenceChanged(ConfigKey configKey, Object newValue) {
        mAccount.setDetail(configKey, newValue.toString());
        updateAccount();
    }

    public void preferenceChanged(ConfigKey configKey, Object newValue) {
        if (configKey == ConfigKey.AUDIO_PORT_MAX || configKey == ConfigKey.AUDIO_PORT_MIN) {
            newValue = adjustRtpRange(Integer.valueOf((String) newValue));
        }
        mAccount.setDetail(configKey, newValue.toString());
        updateAccount();
    }

    private void updateAccount() {
        mAccountService.setCredentials(mAccount.getAccountID(), mAccount.getCredentialsHashMapList());
        mAccountService.setAccountDetails(mAccount.getAccountID(), mAccount.getDetails());
    }

    private String adjustRtpRange(int newValue) {
        if (newValue < 1024) {
            return "1024";
        }

        if (newValue > 65534) {
            return "65534";
        }

        return String.valueOf(newValue);
    }

    private ArrayList<CharSequence> getNetworkInterfaces() {
        ArrayList<CharSequence> result = new ArrayList<>();
        result.add("default");
        try {

            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements(); ) {
                NetworkInterface i = list.nextElement();
                if (i.isUp()) {
                    result.add(i.getDisplayName());
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Error enumerating interfaces: ", e);
        }
        return result;
    }
}
