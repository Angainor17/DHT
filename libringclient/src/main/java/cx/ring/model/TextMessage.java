package cx.ring.model;

public class TextMessage extends Interaction {

    private boolean mNotified;

    public TextMessage(String author, String account, String daemonId, ConversationHistory conversation, String message) {
        mAuthor = author;
        mAccount = account;
        if (daemonId != null) {
            try {
                mDaemonId = Long.parseLong(daemonId);
            } catch (NumberFormatException e) {
                try {
                    mDaemonId = Long.parseLong(daemonId, 16);
                } catch (NumberFormatException e2) {
                    mDaemonId = 0L;
                }
            }
        }
        mTimestamp = System.currentTimeMillis();
        mType = InteractionType.TEXT.toString();
        mConversation = conversation;
        mIsIncoming = author != null;
        mBody = message;
    }

    public TextMessage(Interaction interaction) {
        mId = interaction.getId();
        mAuthor = interaction.getAuthor();
        mTimestamp = interaction.getTimestamp();
        mType = interaction.getType().toString();
        mStatus = interaction.getStatus().toString();
        mConversation = interaction.getConversation();
        mIsIncoming = mAuthor != null;
        mDaemonId = interaction.getDaemonId();
        mBody = interaction.getBody();
        mIsRead = interaction.isRead() ? 1 : 0;
        mAccount = interaction.getAccount();
        mContact = interaction.getContact();
    }

    public boolean isNotified() {
        return mNotified;
    }

    public void setNotified(boolean notified) {
        mNotified = notified;
    }

    public void setStatus(int status) {
        if (status == 3)
            mIsRead = 1;

        mStatus = InteractionStatus.fromIntTextMessage(status).toString();
    }


}
