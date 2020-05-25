package cx.ring.views;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import cx.ring.R;
import cx.ring.adapters.ConversationAdapter;
import cx.ring.utils.UiUpdater;
import io.reactivex.disposables.CompositeDisposable;

public class ConversationViewHolder extends RecyclerView.ViewHolder {
    public View mItem;
    public TextView mMsgTxt;
    public TextView mMsgDetailTxt;
    public TextView mMsgDetailTxtPerm;
    public ImageView mAvatar;
    public ImageView mImage;
    public ImageView mStatusIcon;
    public ImageView mIcon;
    public TextView mHistTxt;
    public TextView mHistDetailTxt;
    public View mLayout;
    public ViewGroup mAnswerLayout;
    public View btnAccept;
    public View btnRefuse;
    public ProgressBar progress;
    public MediaPlayer player;
    public TextureView video;
    public Surface surface = null;
    public String mCid;
    public UiUpdater updater;
    public LinearLayout mCallInfoLayout, mFileInfoLayout, mAudioInfoLayout;
    public ValueAnimator animator;

    public final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ConversationViewHolder(ViewGroup v, ConversationAdapter.MessageType type) {
        super(v);
        if (type == ConversationAdapter.MessageType.CONTACT_EVENT) {
            mMsgTxt = v.findViewById(R.id.contact_event_txt);
            mMsgDetailTxt = v.findViewById(R.id.contact_event_details_txt);
        } else {
            switch (type) {
                // common layout elements
                case INCOMING_TEXT_MESSAGE:
                case OUTGOING_TEXT_MESSAGE:
                    mItem = v.findViewById(R.id.txt_entry);
                    mMsgTxt = v.findViewById(R.id.msg_txt);
                    mMsgDetailTxt = v.findViewById(R.id.msg_details_txt);
                    mMsgDetailTxtPerm = v.findViewById(R.id.msg_details_txt_perm);
                    break;
                case INCOMING_FILE:
                case OUTGOING_FILE:
                    mMsgTxt = v.findViewById(R.id.call_hist_filename);
                    mMsgDetailTxt = v.findViewById(R.id.file_details_txt);
                    mLayout = v.findViewById(R.id.file_layout);
                    mFileInfoLayout = v.findViewById(R.id.fileInfoLayout);
                    mIcon = v.findViewById(R.id.file_icon);
                    progress = v.findViewById(R.id.progress);
                    mAnswerLayout = v.findViewById(R.id.llAnswer);
                    btnAccept = v.findViewById(R.id.btnAccept);
                    btnRefuse = v.findViewById(R.id.btnRefuse);
                    mMsgDetailTxtPerm = v.findViewById(R.id.msg_details_txt_perm);
                    break;
                case INCOMING_IMAGE:
                case OUTGOING_IMAGE:
                    mImage = v.findViewById(R.id.image);
                    mAnswerLayout = v.findViewById(R.id.imageLayout);
                    mMsgDetailTxtPerm = v.findViewById(R.id.msg_details_txt_perm);
                    mMsgDetailTxt = v.findViewById(R.id.msg_details_txt);
                    break;

                case COMPOSING_INDICATION:
                    mStatusIcon = v.findViewById(R.id.status_icon);
            }
        }
    }
}
