package cx.ring.services;

import java.util.List;
import java.util.Map;

import cx.ring.model.CallContact;
import cx.ring.utils.Tuple;
import ezvcard.VCard;
import io.reactivex.Single;

public abstract class VCardService {

    public static final int MAX_SIZE_SIP = 256 * 1024;
    public static final int MAX_SIZE_REQUEST = 64 * 1024;

    public abstract Single<VCard> loadSmallVCard(String accountId, int maxSize);

    public abstract Single<Tuple<String, Object>> loadVCardProfile(VCard vcard);

    public abstract void migrateContact(Map<String, CallContact> contacts, String accountId);

    public abstract void migrateProfiles(List<String> accountIds);

    public abstract void deleteLegacyProfiles();

}
