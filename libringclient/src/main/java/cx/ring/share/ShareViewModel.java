package cx.ring.share;

import cx.ring.model.Account;
import cx.ring.utils.QRCodeUtils;

public class ShareViewModel {

    private final String shareUri;
    private final String displayUri;
    private final Account mAccount;

    public ShareViewModel(Account account) {
        shareUri = account.getUri();
        displayUri = account.getDisplayUri();
        mAccount = account;
    }

    public QRCodeUtils.QRCodeData getAccountQRCodeData(final int foregroundColor, final int backgroundColor) {
        return QRCodeUtils.encodeStringAsQRCodeData(shareUri, foregroundColor, backgroundColor);
    }

    public String getAccountShareUri() {
        return shareUri;
    }

    public String getAccountDisplayUri() {
        return displayUri;
    }

    public Account getAccount() {
        return mAccount;
    }


}
