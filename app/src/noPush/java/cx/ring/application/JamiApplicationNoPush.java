package cx.ring.application;

public class JamiApplicationNoPush extends JamiApplication {

    @Override
    public String getPushToken() {
        return "";
    }

}
