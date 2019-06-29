package com.acafela.harmony.communicator;

import com.acafela.harmony.sip.SipMessage;

import static com.acafela.harmony.codec.audio.AudioMediaFormat.AUDIO_SAMPLE_RATE;

public interface DataCommunicator {
    static final boolean isAudioHeader = true;
    static final int AUDIO_HEADER_SIZE= 3;
    static final int MILLISECONDS_IN_A_SECOND = 1000;
    static final int SAMPLE_INTERVAL = 20;   // Milliseconds
    static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    static final int RAW_BUFFER_SIZE = AUDIO_SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    static final int MAX_AUDIO_SEQNO = 10000;

    public boolean setSession(String ip,int port);
    public boolean startCommunicator();
    public boolean endCommunicator();
    public SipMessage.SessionType getType();
    public int getPortNum();
}
