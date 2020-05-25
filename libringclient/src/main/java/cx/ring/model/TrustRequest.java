package cx.ring.model;

import java.util.Map;
import java.util.Random;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class TrustRequest {
    private static final String TAG = TrustRequest.class.getSimpleName();

    private String mAccountId;
    private String mContactUsername = null;
    private String mContactId;
    private VCard mVcard;
    private String mMessage;
    private long mTimestamp;
    private int mUuid;
    private boolean mUsernameResolved = false;

    public TrustRequest(String accountId, String contact, long received, String payload) {
        mAccountId = accountId;
        mContactId = contact;
        mTimestamp = received;
        mVcard = Ezvcard.parse(payload).first();
        mMessage = null;
        mUuid = new Random().nextInt();
    }

    public TrustRequest(String accountId, Map<String, String> info) {
        this(accountId, info.get("from"), Long.decode(info.get("received")) * 1000L, info.get("payload"));
    }

    public int getUuid() {
        return mUuid;
    }

    public String getAccountId() {
        return mAccountId;
    }

    public String getContactId() {
        return mContactId;
    }

    public String getFullname() {
        String fullname = "";
        if (mVcard != null && mVcard.getFormattedName() != null) {
            fullname = mVcard.getFormattedName().getValue();
        }
        return fullname;
    }

    public String getDisplayname() {
        boolean hasUsername = mContactUsername != null && !mContactUsername.isEmpty();
        return hasUsername ? mContactUsername : mContactId;
    }

    public boolean isNameResolved() {
        return mUsernameResolved;
    }

    public void setUsername(String username) {
        mContactUsername = username;
        mUsernameResolved = true;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public VCard getVCard() {
        return mVcard;
    }

    public void setVCard(VCard vcard) {
        mVcard = vcard;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
