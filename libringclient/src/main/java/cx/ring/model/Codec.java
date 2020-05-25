package cx.ring.model;

import java.util.Map;

public class Codec {

    public enum Type {AUDIO, VIDEO}

    private long mPayload;
    private String mName;
    private Type mType;
    private String mSampleRate;
    private String mBitRate;
    private String mChannels;
    private boolean mIsEnabled;

    public Codec(long i, Map<String, String> audioCodecDetails, boolean enabled) {
        mPayload = i;
        mName = audioCodecDetails.get("CodecInfo.name");
        mType = audioCodecDetails.get("CodecInfo.type").contentEquals("AUDIO") ? Type.AUDIO : Type.VIDEO;
        if (audioCodecDetails.containsKey("CodecInfo.sampleRate")) {
            mSampleRate = audioCodecDetails.get("CodecInfo.sampleRate");
        }
        if (audioCodecDetails.containsKey("CodecInfo.bitrate")) {
            mBitRate = audioCodecDetails.get("CodecInfo.bitrate");
        }
        if (audioCodecDetails.containsKey("CodecInfo.channelNumber")) {
            mChannels = audioCodecDetails.get("CodecInfo.channelNumber");
        }
        mIsEnabled = enabled;
    }

    @Override
    public String toString() {
        return "Codec: " + getName()
                + "\n" + "Payload: " + getPayload()
                + "\n" + "Sample Rate: " + getSampleRate()
                + "\n" + "Bit Rate: " + getBitRate()
                + "\n" + "Channels: " + getChannels();
    }

    public Type getType() {
        return mType;
    }

    public Long getPayload() {
        return mPayload;
    }

    public CharSequence getName() {
        return mName;
    }

    public String getSampleRate() {
        return mSampleRate;
    }

    public String getBitRate() {
        return mBitRate;
    }

    public String getChannels() {
        return mChannels;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void toggleState() {
        mIsEnabled = !mIsEnabled;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Codec && ((Codec) o).mPayload == mPayload;
    }

    public boolean isSpeex() {
        return mName.contentEquals("speex");
    }

}
