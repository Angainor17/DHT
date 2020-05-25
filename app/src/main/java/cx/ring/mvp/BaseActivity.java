package cx.ring.mvp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

public abstract class BaseActivity<T extends RootPresenter> extends AppCompatActivity {
    @Inject
    protected T presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter.bindView(this);
        initPresenter(presenter);
    }

    @Override
    public void onDestroy() {
        presenter.unbindView();

        super.onDestroy();
    }

    protected void initPresenter(T presenter) {

    }
}