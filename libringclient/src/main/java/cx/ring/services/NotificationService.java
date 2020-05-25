package cx.ring.services;

import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.Conversation;
import cx.ring.model.DataTransfer;
import cx.ring.model.Uri;

public interface NotificationService {
    String TRUST_REQUEST_NOTIFICATION_ACCOUNT_ID = "trustRequestNotificationAccountId";
    String TRUST_REQUEST_NOTIFICATION_FROM = "trustRequestNotificationFrom";
    String KEY_NOTIFICATION_ID = "notificationId";

    void showTextNotification(String accountId, Conversation conversation);

    void cancelTextNotification(Uri contact);

    void showIncomingTrustRequestNotification(Account account);

    void cancelTrustRequestNotification(String accountID);

    void showFileTransferNotification(DataTransfer info, CallContact contact);

    void cancelFileNotification(int id, boolean isMigratingToService);

    void updateNotification(Object notification, int notificationId);

    Object getServiceNotification();

    void handleDataTransferNotification(DataTransfer transfer, CallContact contact, boolean remove);

    void removeTransferNotification(long transferId);

    Object getDataTransferNotification(int notificationId);

    void onConnectionUpdate(Boolean b);

    void showLocationNotification(Account first, CallContact contact);

    void cancelLocationNotification(Account first, CallContact contact);

}
