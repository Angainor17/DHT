package cx.ring.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.daemon.IntVect;
import cx.ring.daemon.Ringservice;
import cx.ring.daemon.RingserviceJNI;
import cx.ring.daemon.StringMap;
import cx.ring.daemon.UintVect;
import cx.ring.model.Conference;
import cx.ring.model.SipCall;
import cx.ring.utils.Log;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public abstract class HardwareService {

    private static final String TAG = HardwareService.class.getSimpleName();

    @Inject
    @Named("DaemonExecutor")
    ScheduledExecutorService mExecutor;

    @Inject
    DeviceRuntimeService mDeviceRuntimeService;

    @Inject
    PreferencesService mPreferenceService;

    @Inject
    @Named("UiScheduler")
    protected Scheduler mUiScheduler;

    public static class VideoEvent {
        public boolean start = false;
        public boolean started = false;
        public int w = 0, h = 0;
        public int rot = 0;
        public String callId = null;
    }

    public static class BluetoothEvent {
        public boolean connected;
    }

    public enum AudioOutput {
        INTERNAL, SPEAKERS, BLUETOOTH
    }

    public static class AudioState {
        private final AudioOutput outputType;
        private final String outputName;

        AudioState() {
            outputType = AudioOutput.INTERNAL;
            outputName = null;
        }

        AudioState(AudioOutput ot) {
            outputType = ot;
            outputName = null;
        }

        AudioState(AudioOutput ot, String name) {
            outputType = ot;
            outputName = name;
        }

        public AudioOutput getOutputType() {
            return outputType;
        }

        public String getOutputName() {
            return outputName;
        }
    }

    protected final Subject<VideoEvent> videoEvents = PublishSubject.create();
    protected final Subject<BluetoothEvent> bluetoothEvents = PublishSubject.create();
    protected final Subject<AudioState> audioStateSubject = BehaviorSubject.createDefault(new AudioState());
    protected final Subject<Boolean> connectivityEvents = BehaviorSubject.create();

    public Observable<VideoEvent> getVideoEvents() {
        return videoEvents;
    }

    public Observable<BluetoothEvent> getBluetoothEvents() {
        return bluetoothEvents;
    }

    public Observable<AudioState> getAudioState() {
        return audioStateSubject;
    }

    public Observable<Boolean> getConnectivityState() {
        return connectivityEvents;
    }

    public abstract Completable initVideo();

    public abstract boolean isVideoAvailable();

    public abstract void updateAudioState(SipCall.CallStatus state, boolean incomingCall, boolean isOngoingVideo);

    public abstract void closeAudioState();

    public abstract boolean isSpeakerPhoneOn();

    public abstract void toggleSpeakerphone(boolean checked);

    public abstract void startRinging();

    public abstract void stopRinging();

    public abstract void abandonAudioFocus();

    public abstract void decodingStarted(String id, String shmPath, int width, int height, boolean isMixer);

    public abstract void decodingStopped(String id, String shmPath, boolean isMixer);

    public abstract void getCameraInfo(String camId, IntVect formats, UintVect sizes, UintVect rates);

    public abstract void setParameters(String camId, int format, int width, int height, int rate);

    public abstract void startCapture(String camId);

    public abstract boolean startScreenShare(Object mediaProjection);

    public abstract boolean hasMicrophone();

    public abstract void stopCapture();

    public abstract void endCapture();

    public abstract void stopScreenShare();

    public abstract void requestKeyFrame();

    public abstract void setBitrate(String device, int bitrate);

    public abstract void addVideoSurface(String id, Object holder);

    public abstract void updateVideoSurfaceId(String currentId, String newId);

    public abstract void removeVideoSurface(String id);

    public abstract void addPreviewVideoSurface(Object holder, Conference conference);

    public abstract void updatePreviewVideoSurface(Conference conference);

    public abstract void removePreviewVideoSurface();

    public abstract void switchInput(String id, boolean setDefaultCamera);

    public abstract void setPreviewSettings();

    public abstract boolean hasCamera();

    public abstract int getCameraCount();

    public abstract boolean isPreviewFromFrontCamera();

    public abstract boolean shouldPlaySpeaker();

    public void connectivityChanged(boolean isConnected) {
        Log.i(TAG, "connectivityChange() " + isConnected);
        connectivityEvents.onNext(isConnected);
        mExecutor.execute(Ringservice::connectivityChanged);
    }

    void switchInput(final String id, final String uri) {
        Log.i(TAG, "switchInput() " + uri);
        mExecutor.execute(() -> Ringservice.switchInput(id, uri));
    }

    public void setPreviewSettings(final Map<String, StringMap> cameraMaps) {
        mExecutor.execute(() -> {
            Log.i(TAG, "applySettings() thread running...");
            for (Map.Entry<String, StringMap> entry : cameraMaps.entrySet()) {
                Ringservice.applySettings(entry.getKey(), entry.getValue());
            }
        });
    }

    public long startVideo(final String inputId, final Object surface, final int width, final int height) {
        long inputWindow = RingserviceJNI.acquireNativeWindow(surface);
        if (inputWindow == 0) {
            return inputWindow;
        }

        RingserviceJNI.setNativeWindowGeometry(inputWindow, width, height);
        RingserviceJNI.registerVideoCallback(inputId, inputWindow);

        return inputWindow;
    }

    public void stopVideo(final String inputId, long inputWindow) {
        if (inputWindow == 0) {
            return;
        }
        RingserviceJNI.unregisterVideoCallback(inputId, inputWindow);
        RingserviceJNI.releaseNativeWindow(inputWindow);
    }

    public abstract void setDeviceOrientation(int rotation);

    protected abstract List<String> getVideoDevices();
}
