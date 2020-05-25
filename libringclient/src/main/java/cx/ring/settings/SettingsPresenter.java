package cx.ring.settings;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.facades.ConversationFacade;
import cx.ring.model.Settings;
import cx.ring.mvp.GenericView;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.PreferencesService;
import cx.ring.utils.Log;
import io.reactivex.Scheduler;

public class SettingsPresenter extends RootPresenter<GenericView<Settings>> {

    private final PreferencesService mPreferencesService;
    private final Scheduler mUiScheduler;
    private final ConversationFacade mConversationFacade;

    private final static String TAG = SettingsPresenter.class.getSimpleName();


    @Inject
    public SettingsPresenter(PreferencesService preferencesService, ConversationFacade conversationFacade, @Named("UiScheduler") Scheduler uiScheduler) {
        mPreferencesService = preferencesService;
        mConversationFacade = conversationFacade;
        mUiScheduler = uiScheduler;
    }

    @Override
    public void bindView(GenericView<Settings> view) {
        super.bindView(view);
        mCompositeDisposable.add(mPreferencesService.getSettingsSubject()
                .subscribeOn(mUiScheduler)
                .subscribe(settings -> getView().showViewModel(settings)));
    }

    public void loadSettings() {
        mPreferencesService.getSettings();
    }

    public void saveSettings(Settings settings) {
        mPreferencesService.setSettings(settings);
    }

    public void clearHistory() {
        mCompositeDisposable.add(mConversationFacade.clearAllHistory().subscribe(() -> {
        }, e -> Log.e(TAG, "Error clearing app history", e)));
    }

    public void setDarkMode(boolean isChecked) {
        mPreferencesService.setDarkMode(isChecked);
    }

    public boolean getDarkMode() {
        return mPreferencesService.getDarkMode();
    }
}
