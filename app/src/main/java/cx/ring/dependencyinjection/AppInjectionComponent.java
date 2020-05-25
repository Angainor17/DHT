package cx.ring.dependencyinjection;

import javax.inject.Singleton;

import cx.ring.account.AccountCreationFragment;
import cx.ring.account.AccountEditionFragment;
import cx.ring.account.AccountSummaryFragment;
import cx.ring.account.AccountWizardActivity;
import cx.ring.account.ProfileCreationFragment;
import cx.ring.account.RegisterNameDialog;
import cx.ring.application.JamiApplication;
import cx.ring.client.ContactDetailsActivity;
import cx.ring.client.ConversationSelectionActivity;
import cx.ring.client.HomeActivity;
import cx.ring.contactrequests.ContactRequestsFragment;
import cx.ring.facades.ConversationFacade;
import cx.ring.fragments.AdvancedAccountFragment;
import cx.ring.fragments.ConversationFragment;
import cx.ring.fragments.GeneralAccountFragment;
import cx.ring.fragments.SecurityAccountFragment;
import cx.ring.fragments.ShareWithFragment;
import cx.ring.fragments.SmartListFragment;
import cx.ring.history.DatabaseHelper;
import cx.ring.service.AppJobService;
import cx.ring.service.BootReceiver;
import cx.ring.service.DRingService;
import cx.ring.services.AccountService;
import cx.ring.services.AppChooserTargetService;
import cx.ring.services.CallService;
import cx.ring.services.ConferenceService;
import cx.ring.services.ContactServiceImpl;
import cx.ring.services.DaemonService;
import cx.ring.services.DataTransferService;
import cx.ring.services.DeviceRuntimeServiceImpl;
import cx.ring.services.HardwareService;
import cx.ring.services.HistoryServiceImpl;
import cx.ring.services.LocationSharingService;
import cx.ring.services.NotificationServiceImpl;
import cx.ring.services.SharedPreferencesServiceImpl;
import cx.ring.share.ShareFragment;
import dagger.Component;

@Singleton
@Component(modules = {AppInjectionModule.class, ServiceInjectionModule.class})
public interface AppInjectionComponent {
    void inject(JamiApplication app);

    void inject(HomeActivity activity);

    void inject(DatabaseHelper helper);

    void inject(AccountWizardActivity activity);

    void inject(AccountEditionFragment activity);

    void inject(AccountSummaryFragment fragment);

    void inject(SmartListFragment fragment);

    void inject(ConversationSelectionActivity fragment);

    void inject(AccountCreationFragment fragment);

    void inject(SecurityAccountFragment fragment);

    void inject(ShareFragment fragment);

    void inject(ProfileCreationFragment fragment);

    void inject(RegisterNameDialog dialog);

    void inject(ConversationFragment fragment);

    void inject(ContactRequestsFragment fragment);

    void inject(DRingService service);

    void inject(DeviceRuntimeServiceImpl service);

    void inject(DaemonService service);

    void inject(CallService service);

    void inject(ConferenceService service);

    void inject(AccountService service);

    void inject(HardwareService service);

    void inject(SharedPreferencesServiceImpl service);

    void inject(HistoryServiceImpl service);

    void inject(ContactServiceImpl service);

    void inject(NotificationServiceImpl service);

    void inject(ConversationFacade service);

    void inject(DataTransferService service);

    void inject(BootReceiver receiver);

    void inject(AdvancedAccountFragment fragment);

    void inject(GeneralAccountFragment fragment);

    void inject(AppChooserTargetService service);

    void inject(AppJobService service);

    void inject(ShareWithFragment fragment);

    void inject(ContactDetailsActivity fragment);

    void inject(LocationSharingService service);
}
