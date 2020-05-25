package cx.ring.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.daemon.Ringservice;
import cx.ring.daemon.StringVect;
import cx.ring.utils.Log;

public class ConferenceService {

    private final static String TAG = ConferenceService.class.getName();

    @Inject
    @Named("DaemonExecutor")
    ScheduledExecutorService mExecutor;

    @Inject
    DeviceRuntimeService mDeviceRuntimeService;

    public void removeConference(final String confId) {
        mExecutor.execute(() -> Ringservice.removeConference(confId));
    }

    public void joinParticipant(final String selCallId, final String dragCallId) {
        mExecutor.execute(() -> Ringservice.joinParticipant(selCallId, dragCallId));
    }

    public void addParticipant(final String callId, final String confId) {
        mExecutor.execute(() -> Ringservice.addParticipant(callId, confId));
    }

    public void addMainParticipant(final String confId) {
        mExecutor.execute(() -> Ringservice.addMainParticipant(confId));
    }

    public void detachParticipant(final String callId) {
        mExecutor.execute(() -> Ringservice.detachParticipant(callId));
    }

    public void joinConference(final String selConfId, final String dragConfId) {
        mExecutor.execute(() -> Ringservice.joinConference(selConfId, dragConfId));
    }

    public void hangUpConference(final String confId) {
        mExecutor.execute(() -> Ringservice.hangUpConference(confId));
    }

    public void holdConference(final String confId) {
        mExecutor.execute(() -> Ringservice.holdConference(confId));
    }

    public void unholdConference(final String confId) {
        mExecutor.execute(() -> Ringservice.unholdConference(confId));
    }

    public boolean isConferenceParticipant(final String callId) {
        try {
            return mExecutor.submit(() -> {
                Log.i(TAG, "isConferenceParticipant() running...");
                return Ringservice.isConferenceParticipant(callId);
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Error running isConferenceParticipant()", e);
        }
        return false;
    }

    public Map<String, ArrayList<String>> getConferenceList() {
        try {
            return mExecutor.submit(() -> {
                Log.i(TAG, "getConferenceList() running...");
                StringVect callIds = Ringservice.getCallList();
                HashMap<String, ArrayList<String>> confs = new HashMap<>(callIds.size());
                for (int i = 0; i < callIds.size(); i++) {
                    String callId = callIds.get(i);
                    String confId = Ringservice.getConferenceId(callId);
                    Map<String, String> callDetails = Ringservice.getCallDetails(callId).toNative();

                    //todo remove condition when callDetails does not contains sips ids anymore
                    if (!callDetails.get("PEER_NUMBER").contains("sips")) {
                        if (confId == null || confId.isEmpty()) {
                            confId = callId;
                        }
                        ArrayList<String> calls = confs.get(confId);
                        if (calls == null) {
                            calls = new ArrayList<>();
                            confs.put(confId, calls);
                        }
                        calls.add(callId);
                    }
                }
                return confs;
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Error running isConferenceParticipant()", e);
        }
        return null;
    }

    public List<String> getParticipantList(final String confId) {
        try {
            return mExecutor.submit(() -> {
                Log.i(TAG, "getParticipantList() running...");
                return new ArrayList<>(Ringservice.getParticipantList(confId));
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Error running getParticipantList()", e);
        }
        return null;
    }

    public String getConferenceId(String callId) {
        return Ringservice.getConferenceId(callId);
    }

    public String getConferenceDetails(final String callId) {
        try {
            return mExecutor.submit(() -> {
                Log.i(TAG, "getConferenceDetails() thread running...");
                return Ringservice.getConferenceDetails(callId).get("CONF_STATE");
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Error running getParticipantList()", e);
        }
        return null;
    }

    public Map<String, String> getConference(final String id) {
        try {
            return mExecutor.submit(() -> {
                Log.i(TAG, "getCredentials() thread running...");
                return Ringservice.getConferenceDetails(id).toNative();
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Error running getParticipantList()", e);
        }
        return null;
    }

    void conferenceCreated(final String confId) {
        Log.d(TAG, "conference created: " + confId);
    }

    void conferenceRemoved(String confId) {
        Log.d(TAG, "conference removed: " + confId);
    }

    void conferenceChanged(String confId, String state) {
        Log.d(TAG, "conference changed: " + confId + ", " + state);
    }

}
