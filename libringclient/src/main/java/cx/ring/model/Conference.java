package cx.ring.model;

import java.util.ArrayList;
import java.util.List;

public class Conference {

    private String mId;
    private SipCall.CallStatus mConfState;
    private final ArrayList<SipCall> mParticipants;
    private boolean mRecording;

    public Conference(SipCall call) {
        this(call.getDaemonIdString());
        mParticipants.add(call);
    }

    public Conference(String cID) {
        mId = cID;
        mParticipants = new ArrayList<>();
        mRecording = false;
    }

    public Conference(Conference c) {
        mId = c.mId;
        mConfState = c.mConfState;
        mParticipants = new ArrayList<>(c.mParticipants);
        mRecording = c.mRecording;
    }

    public boolean isRinging() {
        return !mParticipants.isEmpty() && mParticipants.get(0).isRinging();
    }

    public boolean isConference() {
        return mParticipants.size() > 1;
    }

    public SipCall getCall() {
        if (!isConference() && !mParticipants.isEmpty()) {
            return mParticipants.get(0);
        }
        return null;
    }

    public String getId() {
        if (mParticipants.size() == 1) {
            return mParticipants.get(0).getDaemonIdString();
        } else {
            return mId;
        }
    }

    public String getConfId() {
        return mId;
    }

    public SipCall.CallStatus getState() {
        if (mParticipants.size() == 1) {
            return mParticipants.get(0).getCallStatus();
        }
        return mConfState;
    }

    public List<SipCall> getParticipants() {
        return mParticipants;
    }

    public void addParticipant(SipCall part) {
        mParticipants.add(part);
    }

    public boolean removeParticipant(SipCall toRemove) {
        return mParticipants.remove(toRemove);
    }

    public boolean contains(String callID) {
        for (SipCall participant : mParticipants) {
            if (participant.getDaemonIdString().contentEquals(callID))
                return true;
        }
        return false;
    }

    public SipCall getCallById(String callID) {
        for (SipCall participant : mParticipants) {
            if (participant.getDaemonIdString().contentEquals(callID))
                return participant;
        }
        return null;
    }

    public boolean isIncoming() {
        return mParticipants.size() == 1 && mParticipants.get(0).isIncoming();
    }

    public boolean isOnGoing() {
        return mParticipants.size() == 1 && mParticipants.get(0).isOnGoing() || mParticipants.size() > 1;
    }

    public boolean hasVideo() {
        for (SipCall call : mParticipants)
            if (!call.isAudioOnly())
                return true;
        return false;
    }

    public long getTimestampStart() {
        long t = Long.MAX_VALUE;
        for (SipCall call : mParticipants)
            t = Math.min(call.getTimestamp(), t);
        return t;
    }

    public void removeParticipants() {
        mParticipants.clear();
    }
}
