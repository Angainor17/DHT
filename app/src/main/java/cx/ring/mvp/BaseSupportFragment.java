package cx.ring.mvp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import cx.ring.R;
import cx.ring.model.Error;

public abstract class BaseSupportFragment<T extends RootPresenter> extends Fragment implements BaseView {

    protected static final String TAG = BaseSupportFragment.class.getSimpleName();

    @Inject
    protected T presenter;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        //Be sure to do the injection in onCreateView method
        if (presenter != null) {
            presenter.bindView(this);
            initPresenter(presenter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null)
            presenter.unbindView();
    }

    public void displayErrorToast(Error error) {
        String errorString;
        switch (error) {
            case INVALID_FILE:
                errorString = getString(R.string.invalid_file);
                break;
            case NOT_ABLE_TO_WRITE_FILE:
                errorString = getString(R.string.not_able_to_write_file);
                break;
            case NO_SPACE_LEFT:
                errorString = getString(R.string.no_space_left_on_device);
                break;
            default:
                errorString = getString(R.string.generic_error);
                break;
        }

        Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show();
    }

    protected void initPresenter(T presenter) {
    }

    protected void replaceFragmentWithSlide(Fragment fragment, @IdRes int content) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right,
                        R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(content, fragment, TAG)
                .addToBackStack(TAG)
                .commit();
    }

    protected void replaceFragment(Fragment fragment, @IdRes int content) {
        getFragmentManager()
                .beginTransaction()
                .replace(content, fragment, TAG)
                .addToBackStack(TAG)
                .commit();
    }
}
