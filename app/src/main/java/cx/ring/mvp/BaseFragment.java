package cx.ring.mvp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import cx.ring.R;
import cx.ring.model.Error;

public abstract class BaseFragment<T extends RootPresenter> extends Fragment implements BaseView {

    protected static final String TAG = BaseFragment.class.getSimpleName();

    @Inject
    protected T presenter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Be sure to do the injection in onCreateView method
        presenter.bindView(this);
        initPresenter(presenter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

        Toast.makeText(getActivity(), errorString, Toast.LENGTH_LONG).show();
    }

    protected void initPresenter(T presenter) {
    }
}
