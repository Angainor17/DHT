package cx.ring.mvp;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;

public abstract class RootPresenter<T> {

    protected CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public RootPresenter() {

    }

    private WeakReference<T> mView;

    public void bindView(T view) {
        mView = new WeakReference<>(view);
    }

    public void unbindView() {
        if (mView != null) {
            mView.clear();
        }

        mView = null;
        mCompositeDisposable.dispose();
    }

    public T getView() {
        if (mView != null) {
            return mView.get();
        }

        return null;
    }

}


