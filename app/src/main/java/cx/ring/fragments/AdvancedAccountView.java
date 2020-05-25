package cx.ring.fragments;

import java.util.ArrayList;

import cx.ring.model.AccountConfig;

public interface AdvancedAccountView {

    void initView(AccountConfig config, ArrayList<CharSequence> networkInterfaces);

}
