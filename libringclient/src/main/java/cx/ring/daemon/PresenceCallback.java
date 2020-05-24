/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.daemon;

public class PresenceCallback {
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected PresenceCallback(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(PresenceCallback obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    @SuppressWarnings("deprecation")
    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (swigCPtr != 0) {
            if (swigCMemOwn) {
                swigCMemOwn = false;
                RingserviceJNI.delete_PresenceCallback(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    protected void swigDirectorDisconnect() {
        swigCMemOwn = false;
        delete();
    }

    public void swigReleaseOwnership() {
        swigCMemOwn = false;
        RingserviceJNI.PresenceCallback_change_ownership(this, swigCPtr, false);
    }

    public void swigTakeOwnership() {
        swigCMemOwn = true;
        RingserviceJNI.PresenceCallback_change_ownership(this, swigCPtr, true);
    }

    public void newServerSubscriptionRequest(String arg0) {
        if (getClass() == PresenceCallback.class)
            RingserviceJNI.PresenceCallback_newServerSubscriptionRequest(swigCPtr, this, arg0);
        else
            RingserviceJNI.PresenceCallback_newServerSubscriptionRequestSwigExplicitPresenceCallback(swigCPtr, this, arg0);
    }

    public void serverError(String arg0, String arg1, String arg2) {
        if (getClass() == PresenceCallback.class)
            RingserviceJNI.PresenceCallback_serverError(swigCPtr, this, arg0, arg1, arg2);
        else
            RingserviceJNI.PresenceCallback_serverErrorSwigExplicitPresenceCallback(swigCPtr, this, arg0, arg1, arg2);
    }

    public void newBuddyNotification(String arg0, String arg1, int arg2, String arg3) {
        if (getClass() == PresenceCallback.class)
            RingserviceJNI.PresenceCallback_newBuddyNotification(swigCPtr, this, arg0, arg1, arg2, arg3);
        else
            RingserviceJNI.PresenceCallback_newBuddyNotificationSwigExplicitPresenceCallback(swigCPtr, this, arg0, arg1, arg2, arg3);
    }

    public void nearbyPeerNotification(String arg0, String arg1, int arg2, String arg3) {
        if (getClass() == PresenceCallback.class)
            RingserviceJNI.PresenceCallback_nearbyPeerNotification(swigCPtr, this, arg0, arg1, arg2, arg3);
        else
            RingserviceJNI.PresenceCallback_nearbyPeerNotificationSwigExplicitPresenceCallback(swigCPtr, this, arg0, arg1, arg2, arg3);
    }

    public void subscriptionStateChanged(String arg0, String arg1, int arg2) {
        if (getClass() == PresenceCallback.class)
            RingserviceJNI.PresenceCallback_subscriptionStateChanged(swigCPtr, this, arg0, arg1, arg2);
        else
            RingserviceJNI.PresenceCallback_subscriptionStateChangedSwigExplicitPresenceCallback(swigCPtr, this, arg0, arg1, arg2);
    }

    public PresenceCallback() {
        this(RingserviceJNI.new_PresenceCallback(), true);
        RingserviceJNI.PresenceCallback_director_connect(this, swigCPtr, true, true);
    }

}
