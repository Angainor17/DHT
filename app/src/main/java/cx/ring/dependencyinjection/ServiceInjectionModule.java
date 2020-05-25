package cx.ring.dependencyinjection;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import cx.ring.application.JamiApplication;
import cx.ring.facades.ConversationFacade;
import cx.ring.services.AccountService;
import cx.ring.services.CallService;
import cx.ring.services.ContactService;
import cx.ring.services.ContactServiceImpl;
import cx.ring.services.DaemonService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.DeviceRuntimeServiceImpl;
import cx.ring.services.HardwareService;
import cx.ring.services.HardwareServiceImpl;
import cx.ring.services.HistoryService;
import cx.ring.services.HistoryServiceImpl;
import cx.ring.services.LogService;
import cx.ring.services.LogServiceImpl;
import cx.ring.services.NotificationService;
import cx.ring.services.NotificationServiceImpl;
import cx.ring.services.PreferencesService;
import cx.ring.services.SharedPreferencesServiceImpl;
import cx.ring.services.VCardService;
import cx.ring.services.VCardServiceImpl;
import cx.ring.utils.Log;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Module
public class ServiceInjectionModule {

    private final JamiApplication mJamiApplication;

    public ServiceInjectionModule(JamiApplication app) {
        mJamiApplication = app;
    }

    @Provides
    @Singleton
    PreferencesService provideSettingsService() {
        SharedPreferencesServiceImpl settingsService = new SharedPreferencesServiceImpl();
        mJamiApplication.getInjectionComponent().inject(settingsService);
        return settingsService;
    }

    @Provides
    @Singleton
    HistoryService provideHistoryService() {
        HistoryServiceImpl historyService = new HistoryServiceImpl();
        mJamiApplication.getInjectionComponent().inject(historyService);
        return historyService;
    }

    @Provides
    @Singleton
    LogService provideLogService() {
        LogService service = new LogServiceImpl();
        Log.injectLogService(service);
        return service;
    }

    @Provides
    @Singleton
    NotificationService provideNotificationService() {
        NotificationServiceImpl service = new NotificationServiceImpl();
        mJamiApplication.getInjectionComponent().inject(service);
        service.initHelper();
        return service;
    }

    @Provides
    @Singleton
    DeviceRuntimeService provideDeviceRuntimeService(LogService logService) {
        DeviceRuntimeServiceImpl runtimeService = new DeviceRuntimeServiceImpl();
        mJamiApplication.getInjectionComponent().inject(runtimeService);
        runtimeService.loadNativeLibrary();
        return runtimeService;
    }

    @Provides
    @Singleton
    DaemonService provideDaemonService(DeviceRuntimeService deviceRuntimeService) {
        DaemonService daemonService = new DaemonService(deviceRuntimeService);
        mJamiApplication.getInjectionComponent().inject(daemonService);
        return daemonService;
    }

    @Provides
    @Singleton
    CallService provideCallService() {
        CallService callService = new CallService();
        mJamiApplication.getInjectionComponent().inject(callService);
        return callService;
    }

    @Provides
    @Singleton
    AccountService provideAccountService() {
        AccountService accountService = new AccountService();
        mJamiApplication.getInjectionComponent().inject(accountService);
        return accountService;
    }

    @Provides
    @Singleton
    HardwareService provideHardwareService(Context context) {
        HardwareServiceImpl hardwareService = new HardwareServiceImpl(context);
        mJamiApplication.getInjectionComponent().inject(hardwareService);
        return hardwareService;
    }

    @Provides
    @Singleton
    ContactService provideContactService(PreferencesService sharedPreferencesService) {
        ContactServiceImpl contactService = new ContactServiceImpl();
        mJamiApplication.getInjectionComponent().inject(contactService);
        return contactService;
    }

    @Provides
    @Singleton
    ConversationFacade provideConversationFacade(
            HistoryService historyService,
            CallService callService,
            ContactService contactService,
            AccountService accountService,
            NotificationService notificationService) {
        ConversationFacade conversationFacade = new ConversationFacade(historyService, callService, accountService, contactService, notificationService);
        mJamiApplication.getInjectionComponent().inject(conversationFacade);
        return conversationFacade;
    }

    @Provides
    @Singleton
    VCardService provideVCardService(Context context) {
        return new VCardServiceImpl(context);
    }

    @Provides
    @Named("DaemonExecutor")
    @Singleton
    ScheduledExecutorService provideDaemonExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "DRing"));
    }

    @Provides
    @Named("UiScheduler")
    @Singleton
    Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
