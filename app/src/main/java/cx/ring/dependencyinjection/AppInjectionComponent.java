/*
 *  Copyright (C) 2004-2019 Savoir-faire Linux Inc.
 *
 *  Author: Thibault Wittemberg <thibault.wittemberg@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package cx.ring.dependencyinjection;

import javax.inject.Singleton;

import cx.ring.account.AccountEditionFragment;
import cx.ring.account.AccountSummaryFragment;
import cx.ring.account.AccountWizardActivity;
import cx.ring.account.AccountCreationFragment;
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
import cx.ring.launch.LaunchActivity;
import cx.ring.service.BootReceiver;
import cx.ring.service.CallNotificationService;
import cx.ring.service.DRingService;
import cx.ring.service.JamiJobService;
import cx.ring.services.AccountService;
import cx.ring.services.CallService;
import cx.ring.services.ConferenceService;
import cx.ring.services.ContactServiceImpl;
import cx.ring.services.DaemonService;
import cx.ring.services.DataTransferService;
import cx.ring.services.DeviceRuntimeServiceImpl;
import cx.ring.services.HardwareService;
import cx.ring.services.HistoryServiceImpl;
import cx.ring.services.JamiChooserTargetService;
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

    void inject(CallNotificationService service);

    void inject(DataTransferService service);

    void inject(BootReceiver receiver);

    void inject(AdvancedAccountFragment fragment);

    void inject(GeneralAccountFragment fragment);

    void inject(LaunchActivity activity);

    void inject(JamiChooserTargetService service);

    void inject(JamiJobService service);

    void inject(ShareWithFragment fragment);

    void inject(ContactDetailsActivity fragment);

    void inject(LocationSharingService service);
}
