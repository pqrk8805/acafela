package com.acafela.harmony.communicator;

import com.acafela.harmony.sip.SipMessage;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_SAMPLE_RATE;

public interface DataCommunicator {
    static final boolean isAudioHeader = true;
    static final int AUDIO_HEADER_SIZE= 3;
    static final int MAX_AUDIO_SEQNO = 10000;
    static final int COMBINE_DATA = 3;


    public boolean setSession(String ip,int port);
    public boolean startCommunicator();
    public boolean endCommunicator();
    public SipMessage.SessionType getType();
    public int getPortNum();
}
