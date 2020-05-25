package cx.ring.fragments;

import androidx.annotation.NonNull;

import cx.ring.model.Account;

public interface GeneralAccountView {

    void addJamiPreferences(String accountId);

    void addSipPreferences();

    void accountChanged(@NonNull Account account);

    void finish();
}
