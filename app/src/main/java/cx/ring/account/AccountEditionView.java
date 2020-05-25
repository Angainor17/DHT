package cx.ring.account;

public interface AccountEditionView {

    void exit();

    void displaySummary(String accountId);

    void initViewPager(String accountId);

    void showAdvancedOption(boolean show);
}
