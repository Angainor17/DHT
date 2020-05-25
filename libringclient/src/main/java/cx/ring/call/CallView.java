package cx.ring.call;

import java.util.List;

import cx.ring.model.CallContact;
import cx.ring.model.SipCall;
import cx.ring.model.SipCall.CallStatus;
import cx.ring.services.HardwareService;


public interface CallView {

    void displayContactBubble(boolean display);

    void displayVideoSurface(boolean displayVideoSurface, boolean displayPreviewContainer);

    void displayPreviewSurface(boolean display);

    void displayHangupButton(boolean display);

    void displayDialPadKeyboard();

    void switchCameraIcon(boolean isFront);

    void updateAudioState(HardwareService.AudioState state);

    void updateMenu();

    void updateTime(long duration);

    void updateContactBubble(List<SipCall> contact);

    void updateCallStatus(CallStatus callState);

    void initMenu(boolean isSpeakerOn, boolean displayFlip, boolean canDial, boolean onGoingCall);

    void initNormalStateDisplay(boolean audioOnly, boolean muted);

    void initIncomingCallDisplay();

    void initOutGoingCallDisplay();

    void resetPreviewVideoSize(int previewWidth, int previewHeight, int rot);

    void resetVideoSize(int videoWidth, int videoHeight);

    void goToConversation(String accountId, String conversationId);

    void goToAddContact(CallContact callContact);

    void startAddParticipant(String conferenceId);

    void finish();

    void onUserLeave();

    void enterPipMode(String callId);

    void prepareCall(boolean isIncoming);

    void handleCallWakelock(boolean isAudioOnly);

    void goToContact(String accountId, CallContact contact);
}
