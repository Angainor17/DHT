package cx.ring.launch;

public interface LaunchView {

    void askPermissions(String[] permissionsWeCanAsk);

    void goToHome();

    void goToAccountCreation();
}
