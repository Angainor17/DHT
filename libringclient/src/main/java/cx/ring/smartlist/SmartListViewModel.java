package cx.ring.smartlist;

import cx.ring.model.CallContact;
import cx.ring.model.Interaction;

public class SmartListViewModel {
    private final String accountId;
    private final CallContact contact;
    private final String uuid;
    private final String contactName;
    private final boolean hasUnreadTextMessage;
    private boolean hasOngoingCall;
    private boolean isOnline = false;
    private final Interaction lastEvent;

    public SmartListViewModel(String accountId, CallContact contact, String id, Interaction lastEvent) {
        this.accountId = accountId;
        this.contact = contact;
        this.uuid = id;
        this.contactName = contact.getDisplayName();
        hasUnreadTextMessage = (lastEvent != null) && !lastEvent.isRead();
        this.hasOngoingCall = false;
        this.lastEvent = lastEvent;
        isOnline = contact.isOnline();
    }

    public SmartListViewModel(String accountId, CallContact contact, Interaction lastEvent) {
        this.accountId = accountId;
        this.contact = contact;
        this.uuid = contact.getIds().get(0);
        this.contactName = contact.getDisplayName();
        hasUnreadTextMessage = (lastEvent != null) && !lastEvent.isRead();
        this.hasOngoingCall = false;
        this.lastEvent = lastEvent;
        isOnline = contact.isOnline();
    }

    public CallContact getContact() {
        return contact;
    }

    public String getContactName() {
        return contactName;
    }

    public long getLastInteractionTime() {
        return (lastEvent == null) ? 0 : lastEvent.getTimestamp();
    }

    public boolean hasUnreadTextMessage() {
        return hasUnreadTextMessage;
    }

    public boolean hasOngoingCall() {
        return hasOngoingCall;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setHasOngoingCall(boolean hasOngoingCall) {
        this.hasOngoingCall = hasOngoingCall;
    }

    public Interaction getLastEvent() {
        return lastEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SmartListViewModel))
            return false;
        SmartListViewModel other = (SmartListViewModel) o;
        return contact == other.contact
                && contactName.equals(other.contactName)
                && isOnline == other.isOnline
                && lastEvent == other.lastEvent
                && hasOngoingCall == other.hasOngoingCall
                && hasUnreadTextMessage == other.hasUnreadTextMessage;
    }

    public String getAccountId() {
        return accountId;
    }
}
