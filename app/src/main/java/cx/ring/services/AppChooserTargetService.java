package cx.ring.services;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import cx.ring.application.JamiApplication;
import cx.ring.contacts.AvatarFactory;
import cx.ring.facades.ConversationFacade;
import cx.ring.fragments.ConversationFragment;
import cx.ring.model.CallContact;
import cx.ring.model.Conversation;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AppChooserTargetService extends ChooserTargetService {

    @Inject
    @Singleton
    ConversationFacade conversationFacade;

    private int targetSize;

    @Override
    public void onCreate() {
        super.onCreate();
        JamiApplication.getInstance().startDaemon();
        JamiApplication.getInstance().getInjectionComponent().inject(this);
        targetSize = (int) (AvatarFactory.SIZE_NOTIF * getResources().getDisplayMetrics().density);
    }

    @Override
    public List<ChooserTarget> onGetChooserTargets(ComponentName componentName, IntentFilter intentFilter) {
        return conversationFacade
                .getCurrentAccountSubject()
                .firstOrError()
                .flatMap(a -> a
                        .getConversationsSubject()
                        .firstOrError()
                        .map(conversations -> {
                            List<Future<Bitmap>> futureIcons = new ArrayList<>(conversations.size());
                            for (Conversation conversation : conversations) {
                                CallContact contact = conversation.getContact();
                                futureIcons.add(AvatarFactory.getBitmapAvatar(this, contact, targetSize)
                                        .subscribeOn(Schedulers.computation())
                                        .toFuture());
                            }
                            int i = 0;
                            List<ChooserTarget> choosers = new ArrayList<>(conversations.size());
                            for (Conversation conversation : conversations) {
                                CallContact contact = conversation.getContact();
                                Bundle bundle = new Bundle();
                                bundle.putString(ConversationFragment.KEY_ACCOUNT_ID, a.getAccountID());
                                bundle.putString(ConversationFragment.KEY_CONTACT_RING_ID, contact.getPrimaryNumber());
                                Icon icon = null;
                                try {
                                    icon = Icon.createWithBitmap(futureIcons.get(i).get());
                                } catch (Exception e) {
                                    Log.w("RingChooserService", "Failed to load icon", e);
                                }
                                ChooserTarget target = new ChooserTarget(contact.getDisplayName(), icon, 1.f - (i / (float) conversations.size()), componentName, bundle);
                                choosers.add(target);
                                i++;
                            }
                            return choosers;
                        }))
                .onErrorReturn(e -> new ArrayList<>())
                .blockingGet();
    }
}
