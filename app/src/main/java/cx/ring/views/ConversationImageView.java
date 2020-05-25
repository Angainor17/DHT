package cx.ring.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class ConversationImageView extends ImageView {
    private final int mMaxHeight;

    public ConversationImageView(Context context) {
        super(context);
        mMaxHeight = 0;
    }

    public ConversationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaxHeight = getMaxHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        // Avoid image to take zero space when not loaded yet
        if (measuredHeight == 0) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}