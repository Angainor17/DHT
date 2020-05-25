package cx.ring.contactrequests;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.facades.ConversationFacade;
import cx.ring.model.CallContact;
import cx.ring.model.Conversation;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.ContactService;
import cx.ring.smartlist.SmartListViewModel;
import cx.ring.utils.Log;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

public class ContactRequestsPresenter extends RootPresenter<ContactRequestsView> {

    static private final String TAG = ContactRequestsPresenter.class.getSimpleName();

    private final Scheduler mUiScheduler;
    private final ConversationFacade mConversationFacade;
    private final ContactService mContactService;

    private final CompositeDisposable mContactDisposable = new CompositeDisposable();

    @Inject
    ContactRequestsPresenter(ConversationFacade conversationFacade, ContactService contactService, @Named("UiScheduler") Scheduler scheduler) {
        mConversationFacade = conversationFacade;
        mContactService = contactService;
        mUiScheduler = scheduler;
    }

    private final BehaviorSubject<String> mAccount = BehaviorSubject.create();

    @Override
    public void bindView(ContactRequestsView view) {
        super.bindView(view);
        mCompositeDisposable.add(mConversationFacade.getCurrentAccountSubject()
                .switchMap(a -> a
                        .getPendingSubject()
                        .map(pending -> {
                            ArrayList<SmartListViewModel> viewmodel = new ArrayList<>(pending.size());
                            for (Conversation c : pending) {
                                SmartListViewModel vm = new SmartListViewModel(a.getAccountID(), c.getContact(), c.getContact().getPrimaryNumber(), c.getLastEvent());
                                viewmodel.add(vm);
                            }
                            return viewmodel;
                        }))
                .observeOn(mUiScheduler)
                .subscribe(viewModels -> {
                    getView().updateView(viewModels);
                    CompositeDisposable disposable = new CompositeDisposable();
                    for (SmartListViewModel vm : viewModels) {
                        disposable.add(mContactService.observeContact(vm.getAccountId(), vm.getContact())
                                .observeOn(mUiScheduler)
                                .subscribe(contact -> getView().updateItem(vm), e -> Log.d(TAG, "updateContact onError", e)));
                    }
                    mContactDisposable.clear();
                    mContactDisposable.add(disposable);
                }, e -> Log.d(TAG, "updateList subscribe onError", e)));
    }

    @Override
    public void unbindView() {
        super.unbindView();
        mContactDisposable.dispose();
    }

    public void updateAccount(String accountId) {
        mAccount.onNext(accountId);
    }

    public void contactRequestClicked(String accountId, CallContact contactId) {
        getView().goToConversation(accountId, contactId.getPrimaryNumber());
    }
}
