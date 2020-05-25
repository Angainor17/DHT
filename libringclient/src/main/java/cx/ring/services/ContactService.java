package cx.ring.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.Settings;
import cx.ring.model.Uri;
import ezvcard.VCard;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * This service handles the contacts
 * - Load the contacts stored in the system
 * - Keep a local cache of the contacts
 * - Provide query tools to search contacts by id, number, ...
 */
public abstract class ContactService {
    private final static String TAG = ContactService.class.getSimpleName();

    @Inject
    PreferencesService mPreferencesService;

    @Inject
    DeviceRuntimeService mDeviceRuntimeService;

    @Inject
    AccountService mAccountService;

    public abstract Map<Long, CallContact> loadContactsFromSystem(boolean loadRingContacts, boolean loadSipContacts);

    protected abstract CallContact findContactBySipNumberFromSystem(String number);

    public abstract Completable loadContactData(CallContact callContact, String accountId);

    public abstract void saveVCardContactData(CallContact contact, String accountId, VCard vcard);

    public ContactService() {
    }

    /**
     * Load contacts from system and generate a local contact cache
     *
     * @param loadRingContacts if true, ring contacts will be taken care of
     * @param loadSipContacts  if true, sip contacts will be taken care of
     */
    public Single<Map<Long, CallContact>> loadContacts(final boolean loadRingContacts, final boolean loadSipContacts, final Account account) {
        return Single.fromCallable(() -> {
            Settings settings = mPreferencesService.getSettings();
            if (settings.isAllowSystemContacts() && mDeviceRuntimeService.hasContactPermission()) {
                return loadContactsFromSystem(loadRingContacts, loadSipContacts);
            }
            return new HashMap<>();
        });
    }

    public Observable<CallContact> observeContact(String accountId, CallContact contact) {
        Uri uri = contact.getPrimaryUri();
        String uriString = uri.getRawUriString();
        synchronized (contact) {
            if (contact.getUpdates() == null) {
                contact.setUpdates(contact.getUpdatesSubject()
                        .doOnSubscribe(d -> {
                            mAccountService.subscribeBuddy(accountId, uriString, true);
                            if (!contact.isUsernameLoaded())
                                mAccountService.lookupAddress(accountId, "", uri.getRawRingId());
                            loadContactData(contact, accountId)
                                    .subscribe(() -> {
                                    }, e -> {/*Log.e(TAG, "Error loading contact data: " + e.getMessage())*/});
                        })
                        .doOnDispose(() -> {
                            mAccountService.subscribeBuddy(accountId, uriString, false);
                        })
                        .filter(c -> c.isUsernameLoaded() && c.detailsLoaded)
                        .replay(1)
                        .refCount(20, TimeUnit.SECONDS));
            }
            return contact.getUpdates();
        }
    }

    public Single<CallContact> getLoadedContact(String accountId, CallContact contact) {
        return observeContact(accountId, contact)
                .filter(c -> c.isUsernameLoaded() && c.detailsLoaded)
                .firstOrError();
    }

    public CallContact findContact(Account account, Uri uri) {
        if (uri == null || account == null) return null;

        return account.getContactFromCache(uri);
    }
}