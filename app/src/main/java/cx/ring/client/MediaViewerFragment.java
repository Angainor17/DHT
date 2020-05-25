package cx.ring.client;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.resource.bitmap.CenterInside;

import cx.ring.R;
import cx.ring.utils.GlideApp;
import cx.ring.utils.GlideOptions;

/**
 * A placeholder fragment containing a simple view.
 */
public class MediaViewerFragment extends Fragment {
    private final static String TAG = MediaViewerFragment.class.getSimpleName();

    private Uri mUri = null;

    protected ImageView mImage;

    private final GlideOptions PICTURE_OPTIONS = new GlideOptions().transform(new CenterInside());

    public MediaViewerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_media_viewer, container, false);
        mImage = view.findViewById(R.id.image);
        showImage();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        if (activity == null)
            return;
        mUri = activity.getIntent().getData();
        showImage();
    }

    private void showImage() {
        if (mUri == null) {
            Log.w(TAG, "showImage(): null URI");
            return;
        }
        Activity a = getActivity();
        if (a == null) {
            Log.w(TAG, "showImage(): null Activity");
            return;
        }
        if (mImage == null) {
            Log.w(TAG, "showImage(): null image view");
            return;
        }
        GlideApp.with(a)
                .load(mUri)
                .apply(PICTURE_OPTIONS)
                .into(mImage);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
