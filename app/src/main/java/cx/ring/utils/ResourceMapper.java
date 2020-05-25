package cx.ring.utils;

import android.content.Context;

import cx.ring.R;
import cx.ring.model.Interaction.InteractionStatus;

public class ResourceMapper {

    public static String getReadableFileTransferStatus(Context context, InteractionStatus transferStatus) {
        if (transferStatus == InteractionStatus.TRANSFER_CREATED) {
            return context.getString(R.string.file_transfer_status_created);
        }
        if (transferStatus == InteractionStatus.TRANSFER_AWAITING_PEER) {
            return context.getString(R.string.file_transfer_status_wait_peer_acceptance);
        }
        if (transferStatus == InteractionStatus.TRANSFER_AWAITING_HOST) {
            return context.getString(R.string.file_transfer_status_wait_host_acceptance);
        }
        if (transferStatus == InteractionStatus.TRANSFER_ONGOING) {
            return context.getString(R.string.file_transfer_status_ongoing);
        }
        if (transferStatus == InteractionStatus.TRANSFER_FINISHED) {
            return context.getString(R.string.file_transfer_status_finished);
        }
        if (transferStatus == InteractionStatus.TRANSFER_CANCELED) {
            return context.getString(R.string.file_transfer_status_cancelled);
        }
        if (transferStatus == InteractionStatus.TRANSFER_UNJOINABLE_PEER) {
            return context.getString(R.string.file_transfer_status_unjoinable_peer);
        }
        if (transferStatus == InteractionStatus.TRANSFER_ERROR) {
            return context.getString(R.string.file_transfer_status_error);
        }
        if (transferStatus == InteractionStatus.TRANSFER_TIMEOUT_EXPIRED) {
            return context.getString(R.string.file_transfer_status_timed_out);
        }
        return "";
    }
}
