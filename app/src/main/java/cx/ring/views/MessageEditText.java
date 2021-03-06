package cx.ring.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

public class MessageEditText extends AppCompatEditText {
    static private final String[] SUPPORTED_MIME_TYPES = new String[]{"image/png", "image/jpg", "image/gif", "image/webp"};

    public interface MediaListener {
        void onMedia(InputContentInfoCompat mediaUri);
    }

    private MediaListener listener = null;

    public MessageEditText(Context context) {
        super(context);
    }

    public MessageEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMediaListener(MediaListener l) {
        listener = l;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo, SUPPORTED_MIME_TYPES);
        return InputConnectionCompat.createWrapper(ic, editorInfo, (inputContentInfo, flags, opts) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
                    && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputContentInfo.requestPermission();
                } catch (Exception e) {
                    return false;
                }
            }
            if (listener != null)
                listener.onMedia(inputContentInfo);
            return true;
        });
    }
}
