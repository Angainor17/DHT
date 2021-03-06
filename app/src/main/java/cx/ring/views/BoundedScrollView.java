package cx.ring.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import cx.ring.R;

public class BoundedScrollView extends ScrollView {
    private final int mBoundedWidth;
    private final int mBoundedHeight;

    public BoundedScrollView(Context context) {
        super(context);
        mBoundedWidth = 0;
        mBoundedHeight = 0;
    }

    public BoundedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoundedScrollView);
        mBoundedWidth = typedArray.getDimensionPixelSize(R.styleable.BoundedScrollView_bounded_width, 0);
        mBoundedHeight = typedArray.getDimensionPixelSize(R.styleable.BoundedScrollView_bounded_height, 0);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mBoundedWidth > 0 && mBoundedWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode);
        }
        // Adjust height as necessary
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mBoundedHeight > 0 && mBoundedHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}