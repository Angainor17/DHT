package cx.ring.fragments;

import java.util.ArrayList;

import cx.ring.model.AccountConfig;
import cx.ring.model.AccountCredentials;


public interface SecurityAccountView {

    void removeAllCredentials();

    void addAllCredentials(ArrayList<AccountCredentials> credentials);

    void setDetails(AccountConfig config, String[] tlsMethods);
}
