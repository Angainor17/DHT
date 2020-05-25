package cx.ring.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cx.ring.R;
import cx.ring.model.CallContact;
import cx.ring.model.Conversation;

public class ActionHelper {

    public static final String TAG = ActionHelper.class.getSimpleName();
    public static final int ACTION_COPY = 0;
    public static final int ACTION_CLEAR = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_BLOCK = 3;

    private ActionHelper() {
    }

    public static void launchClearAction(final Context context,
                                         final CallContact callContact,
                                         final Conversation.ConversationActionCallback callback) {
        if (context == null) {
            Log.d(TAG, "launchClearAction: activity is null");
            return;
        }

        if (callContact == null) {
            Log.d(TAG, "launchClearAction: conversation is null");
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.conversation_action_history_clear_title)
                .setMessage(R.string.conversation_action_history_clear_message)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    if (callback != null) {
                        callback.clearConversation(callContact);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
                    /* Terminate with no action */
                })
                .show();
    }

    public static void launchDeleteAction(final Context context,
                                          final CallContact callContact,
                                          final Conversation.ConversationActionCallback callback) {
        if (context == null) {
            Log.d(TAG, "launchDeleteAction: activity is null");
            return;
        }

        if (callContact == null) {
            Log.d(TAG, "launchDeleteAction: conversation is null");
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.conversation_action_remove_this_title)
                .setMessage(R.string.conversation_action_remove_this_message)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    if (callback != null) {
                        callback.removeConversation(callContact);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
                    /* Terminate with no action */
                })
                .show();
    }

    public static void launchCopyNumberToClipboardFromContact(final Context context,
                                                              final CallContact callContact,
                                                              final Conversation.ConversationActionCallback callback) {
        if (callContact == null) {
            Log.d(TAG, "launchCopyNumberToClipboardFromContact: callContact is null");
            return;
        }

        if (context == null) {
            Log.d(TAG, "launchCopyNumberToClipboardFromContact: activity is null");
            return;
        }

        if (callContact.getPhones().isEmpty()) {
            Log.d(TAG, "launchCopyNumberToClipboardFromContact: no number to copy");
            return;
        }

        if (callContact.getPhones().size() == 1 && callback != null) {
            String number = callContact.getPhones().get(0).getNumber().toString();
            callback.copyContactNumberToClipboard(number);
        }
    }

    public static void displayContact(Context context, CallContact contact) {
        if (context == null) {
            Log.d(TAG, "displayContact: context is null");
            return;
        }

        if (contact.getId() != CallContact.UNKNOWN_ID) {
            Log.d(TAG, "displayContact: contact is known, displaying...");
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                android.net.Uri uri = android.net.Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                        String.valueOf(contact.getId()));
                intent.setData(uri);
                context.startActivity(intent);
            } catch (ActivityNotFoundException exc) {
                Log.e(TAG, "Error displaying contact", exc);
            }
        }
    }

    public static String getShortenedNumber(String number) {
        if (number != null && !number.isEmpty() && number.length() > 18) {
            int size = number.length();
            return number.substring(0, 9).concat("\u2026").concat(number.substring(size - 9, size));
        }
        return number;
    }
}
