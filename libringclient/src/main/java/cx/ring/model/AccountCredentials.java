package cx.ring.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AccountCredentials implements Serializable {

    @SuppressWarnings("unused")
    private static final String TAG = AccountCredentials.class.getSimpleName();

    private String mUsername;
    private String mPassword;
    private String mRealm;

    public AccountCredentials(Map<String, String> pref) {
        mUsername = pref.get(ConfigKey.ACCOUNT_USERNAME.key());
        mPassword = pref.get(ConfigKey.ACCOUNT_PASSWORD.key());
        mRealm = pref.get(ConfigKey.ACCOUNT_REALM.key());
    }

    public AccountCredentials(String username, String password, String realm) {
        setUsername(username);
        setPassword(password);
        setRealm(realm);
    }

    public void setUsername(String val) {
        mUsername = val;
    }

    public void setPassword(String val) {
        mPassword = val;
    }

    public void setRealm(String val) {
        mRealm = val;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getRealm() {
        return mRealm;
    }

    public HashMap<String, String> getDetails() {
        HashMap<String, String> details = new HashMap<>();
        details.put(ConfigKey.ACCOUNT_USERNAME.key(), mUsername);
        details.put(ConfigKey.ACCOUNT_PASSWORD.key(), mPassword);
        details.put(ConfigKey.ACCOUNT_REALM.key(), mRealm);
        return details;
    }

    public void setDetail(ConfigKey key, String value) {

        switch (key) {
            case ACCOUNT_USERNAME:
                mUsername = value;
                break;
            case ACCOUNT_PASSWORD:
                mPassword = value;
                break;
            case ACCOUNT_REALM:
                mRealm = value;
                break;
        }
    }
}
