package cx.ring.utils;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import cx.ring.services.AccountService;

public class NameLookupInputHandler {
    private static final int WAIT_DELAY = 350;
    private final WeakReference<AccountService> mAccountService;
    private final String mAccountId;
    private final Timer timer = new Timer(true);
    private NameTask lastTask = null;

    public NameLookupInputHandler(AccountService accountService, String accountId) {
        mAccountService = new WeakReference<>(accountService);
        mAccountId = accountId;
    }

    public void enqueueNextLookup(String text) {
        if (lastTask != null) {
            lastTask.cancel();
        }
        lastTask = new NameTask(text);
        timer.schedule(lastTask, WAIT_DELAY);
    }

    private class NameTask extends TimerTask {
        private final String mTextToLookup;

        NameTask(String name) {
            mTextToLookup = name;
        }

        @Override
        public void run() {
            final AccountService accountService = mAccountService.get();
            if (accountService != null) {
                accountService.lookupName(mAccountId, "", mTextToLookup);
            }
        }
    }
}