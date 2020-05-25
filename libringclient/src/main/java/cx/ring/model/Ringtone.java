package cx.ring.model;

public class Ringtone {


    private String mName, mRingtonePath;
    private boolean isSelected, isPlaying;
    private Object mRingtoneIcon;


    public Ringtone(String name, String path, Object ringtoneIcon) {
        mName = name;
        mRingtonePath = path;
        isSelected = false;
        isPlaying = false;
        mRingtoneIcon = ringtoneIcon;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getRingtonePath() {
        return mRingtonePath;
    }

    public void setRingtonePath(String ringtonePath) {
        mRingtonePath = ringtonePath;
    }

    public Object getRingtoneIcon() {
        return mRingtoneIcon;
    }

    public void setRingtoneIcon(Object ringtoneIcon) {
        mRingtoneIcon = ringtoneIcon;
    }


}
