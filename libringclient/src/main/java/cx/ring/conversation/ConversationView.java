package cx.ring.conversation;

import java.io.File;
import java.util.List;

import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.DataTransfer;
import cx.ring.model.Error;
import cx.ring.model.Interaction;
import cx.ring.mvp.BaseView;

public interface ConversationView extends BaseView {

    void refreshView(List<Interaction> conversation);

    void scrollToEnd();

    void displayContact(CallContact contact);

    void displayErrorToast(Error error);

    void clearMsgEdit();

    void goToHome();

    void goToContactActivity(String accountId, String contactRingId);

    void switchToUnknownView(String name);

    void switchToIncomingTrustRequestView(String message);

    void switchToConversationView();

    void askWriteExternalStoragePermission();

    void openFilePicker();

    void shareFile(File path);

    void openFile(File path);

    void addElement(Interaction e);

    void updateElement(Interaction e);

    void removeElement(Interaction e);

    void setComposingStatus(Account.ComposingStatus composingStatus);

    void setLastDisplayed(Interaction interaction);

    void setConversationColor(int integer);

    void startSaveFile(DataTransfer currentFile, String fileAbsolutePath);

    void hideErrorPanel();

    void displayNetworkErrorPanel();
}
