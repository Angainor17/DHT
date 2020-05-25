package cx.ring.smartlist;

import java.util.List;

import cx.ring.model.CallContact;
import cx.ring.model.Uri;
import cx.ring.mvp.BaseView;

public interface SmartListView extends BaseView {

    void displayContact(CallContact contact);

    void displayNoConversationMessage();

    void displayConversationDialog(SmartListViewModel smartListViewModel);

    void displayClearDialog(CallContact callContact);

    void displayDeleteDialog(CallContact callContact);

    void copyNumber(CallContact callContact);

    void setLoading(boolean display);

    void displayMenuItem();

    void hideSearchRow();

    void hideList();

    void hideNoConversationMessage();

    void updateList(List<SmartListViewModel> smartListViewModels);

    void update(SmartListViewModel model);

    void update(int position);

    void goToConversation(String accountId, Uri contactId);

    void goToQRActivity();

    void goToContact(CallContact callContact);

    void scrollToTop();
}
