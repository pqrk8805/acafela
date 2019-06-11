package com.acafela.harmony.communicator;

import com.acafela.harmony.sip.SipMessage;

public interface DataCommunicator {
    public boolean setSession(String ip,int port);
    public boolean startCommunicator();
    public boolean endCommunicator();
    public SipMessage.SessionType getType();
}
