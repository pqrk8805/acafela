package com.acafela.harmony.communicator;

import com.acafela.harmony.sip.SipMessage;

public interface DataCommunicator {
    boolean isAudioHeader = true;
    int AUDIO_HEADER_SIZE= 3;
    int MAX_AUDIO_SEQNO = 10000;
    int PACKET_SIZE= 48 + AUDIO_HEADER_SIZE;
    int PACKET_TOTAL_SIZE = PACKET_SIZE; //if it's want to add padding, set value over PACKET_SIZE
    int DUPLICATE_COUNT = 2;

    boolean setSession(String ip,int port);
    boolean startCommunicator();
    boolean endCommunicator();
    SipMessage.SessionType getType();
    int getPortNum();
}
