package cx.ring.service;

interface IDRingService {

    boolean isStarted();

    void lookupName(in String account, in String nameserver, in String name);
    void lookupAddress(in String account, in String nameserver, in String address);
    void registerName(in String account, in String password, in String name);

    List getAccountList();
    String addAccount(in Map accountDetails);
    void removeAccount(in String accoundId);
    void setAccountOrder(in String order);
    Map getAccountDetails(in String accountID);
    Map getVolatileAccountDetails(in String accountID);
    Map getAccountTemplate(in String accountType);
    void registerAllAccounts();
    void setAccountDetails(in String accountId, in Map accountDetails);
    void setAccountActive(in String accountId, in boolean active);
    void setAccountsActive(in boolean active);
    List getCredentials(in String accountID);
    void setCredentials(in String accountID, in List creds);
    void setAudioPlugin(in String callID);
    String getCurrentAudioOutputPlugin();
    List getCodecList(in String accountID);
    void setActiveCodecList(in List codecs, in String accountID);
    void exportOnRing(in String accountID, in String password);
    Map getKnownRingDevices(in String accountID);

    Map validateCertificatePath(in String accountID, in String certificatePath, in String privateKeyPath, in String privateKeyPass);
    Map validateCertificate(in String accountID, in String certificateId);
    Map getCertificateDetailsPath(in String certificatePath);
    Map getCertificateDetails(in String certificate);

    /* Mute */
    void setMuted(boolean mute);
    boolean isCaptureMuted();

    /* Security */
    List getTlsSupportedMethods();

    /* DTMF */
    void playDtmf(in String key);

    /* IM */
    void sendTextMessage(in String callID, in String message);
    long sendAccountTextMessage(in String accountid, in String to, in String msg);
    void sendProfile(in String callID, in String accountID);

    void transfer(in String callID, in String to);
    void attendedTransfer(in String transferID, in String targetID);

    /* Conference related methods */

    void removeConference(in String confID);
    void joinParticipant(in String sel_callID, in String drag_callID);

    void addParticipant(in String callID, in String confID);
    void addMainParticipant(in String confID);
    void detachParticipant(in String callID);
    void joinConference(in String sel_confID, in String drag_confID);
    void hangUpConference(in String confID);
    void holdConference(in String confID);
    void unholdConference(in String confID);
    boolean isConferenceParticipant(in String callID);
    Map getConferenceList();
    List getParticipantList(in String confID);
    String getConferenceId(in String callID);
    String getConferenceDetails(in String callID);

    Map getConference(in String id);

    int backupAccounts(in List accountIDs, in String toDir, in String password);
    int restoreAccounts(in String archivePath, in String password);

    void connectivityChanged();
}
