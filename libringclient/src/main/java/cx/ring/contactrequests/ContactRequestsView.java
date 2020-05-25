package cx.ring.contactrequests;

import java.util.List;

import cx.ring.smartlist.SmartListViewModel;

public interface ContactRequestsView {

    void updateView(List<SmartListViewModel> list);

    void updateItem(SmartListViewModel item);

    void goToConversation(String accountId, String contactId);
}
