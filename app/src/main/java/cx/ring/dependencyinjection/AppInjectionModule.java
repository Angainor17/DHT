package cx.ring.dependencyinjection;

import android.content.Context;

import cx.ring.application.JamiApplication;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Module
public class AppInjectionModule {

    private final JamiApplication mJamiApplication;

    public AppInjectionModule(JamiApplication app) {
        mJamiApplication = app;
    }

    @Provides
    JamiApplication provideRingApplication() {
        return mJamiApplication;
    }

    @Provides
    Context provideContext() {
        return mJamiApplication;
    }

    @Provides
    Scheduler provideMainSchedulers() {
        return AndroidSchedulers.mainThread();
    }

}
